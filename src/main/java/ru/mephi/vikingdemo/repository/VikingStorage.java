package ru.mephi.vikingdemo.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.mephi.vikingdemo.model.EquipmentItem;
import ru.mephi.vikingdemo.model.EquipmentItemEntity;
import ru.mephi.vikingdemo.model.Viking;
import ru.mephi.vikingdemo.model.VikingEntity;
import ru.mephi.vikingdemo.model.VikingView;


@Repository
public class VikingStorage {

    private final VikingRepository vikingRepository;
    private final EquipmentItemRepository equipmentItemRepository;
    private final VikingMapper vikingMapper;

    public VikingStorage(
            VikingRepository vikingRepository,
            EquipmentItemRepository equipmentItemRepository,
            VikingMapper vikingMapper
    ) {
        this.vikingRepository = vikingRepository;
        this.equipmentItemRepository = equipmentItemRepository;
        this.vikingMapper = vikingMapper;
    }

    @Transactional
    public VikingView save(Viking viking) {
        Integer vikingId = vikingRepository.save(
                vikingMapper.toVikingEntity(viking)
        );

        List<EquipmentItem> equipment = safeEquipment(viking);
        saveEquipment(vikingId, equipment);

        return vikingMapper.toVikingView(vikingId, withEquipment(viking, equipment));
    }

    public List<VikingView> findAll() {
        List<VikingEntity> vikingEntities = vikingRepository.findAll();
        List<EquipmentItemEntity> equipmentEntities = equipmentItemRepository.findAll();

        Map<Integer, List<EquipmentItemEntity>> equipmentByVikingId = equipmentEntities.stream()
                .collect(Collectors.groupingBy(EquipmentItemEntity::vikingId));

        return vikingEntities.stream()
                .map(vikingEntity -> vikingMapper.toVikingView(
                        vikingEntity,
                        equipmentByVikingId.getOrDefault(vikingEntity.id(), List.of())
                ))
                .toList();
    }

    public Optional<VikingView> findById(int id) {
        return vikingRepository.findById(id)
                .map(vikingEntity -> vikingMapper.toVikingView(
                        vikingEntity,
                        equipmentItemRepository.findByVikingId(id)
                ));
    }

    @Transactional
    public boolean deleteById(int id) {
        return vikingRepository.deleteById(id);
    }

    @Transactional
    public Optional<VikingView> updateById(int id, Viking viking) {
        List<EquipmentItem> equipment = safeEquipment(viking);
        Viking normalizedViking = withEquipment(viking, equipment);

        boolean updated = vikingRepository.update(
                vikingMapper.toVikingEntity(id, normalizedViking)
        );

        if (!updated) {
            return Optional.empty();
        }

        equipmentItemRepository.deleteByVikingId(id);
        saveEquipment(id, equipment);

        return Optional.of(vikingMapper.toVikingView(id, normalizedViking));
    }

    private void saveEquipment(int vikingId, List<EquipmentItem> equipment) {
        for (EquipmentItem item : equipment) {
            equipmentItemRepository.save(
                    vikingMapper.toEquipmentItemEntity(vikingId, item)
            );
        }
    }

    private List<EquipmentItem> safeEquipment(Viking viking) {
        if (viking.equipment() == null) {
            return List.of();
        }

        return viking.equipment();
    }

    private Viking withEquipment(Viking viking, List<EquipmentItem> equipment) {
        return new Viking(
                viking.name(),
                viking.age(),
                viking.heightCm(),
                viking.hairColor(),
                viking.beardStyle(),
                equipment
        );
    }
}
