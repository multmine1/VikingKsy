package ru.mephi.vikingdemo.repository;

import org.springframework.stereotype.Component;
import ru.mephi.vikingdemo.model.EquipmentItem;
import ru.mephi.vikingdemo.model.EquipmentItemEntity;
import ru.mephi.vikingdemo.model.Viking;
import ru.mephi.vikingdemo.model.VikingEntity;
import ru.mephi.vikingdemo.model.VikingView;

import java.util.List;

@Component
public class VikingMapper {

    public VikingEntity toVikingEntity(Viking viking) {
        return toVikingEntity(null, viking);
    }

    public VikingEntity toVikingEntity(Integer id, Viking viking) {
        return new VikingEntity(
                id,
                viking.name(),
                viking.age(),
                viking.heightCm(),
                viking.hairColor(),
                viking.beardStyle(),
                ""
        );
    }

    public EquipmentItemEntity toEquipmentItemEntity(Integer vikingId, EquipmentItem item) {
        return new EquipmentItemEntity(
                null,
                vikingId,
                item.name(),
                item.quality()
        );
    }

    public EquipmentItem toEquipmentItem(EquipmentItemEntity entity) {
        return new EquipmentItem(
                entity.name(),
                entity.quality()
        );
    }

    public Viking toViking(VikingEntity entity, List<EquipmentItemEntity> equipmentEntities) {
        List<EquipmentItem> equipment = equipmentEntities.stream()
                .map(this::toEquipmentItem)
                .toList();

        return new Viking(
                entity.name(),
                entity.age(),
                entity.heightCm(),
                entity.hairColor(),
                entity.beardStyle(),
                equipment
        );
    }

    public VikingView toVikingView(VikingEntity entity, List<EquipmentItemEntity> equipmentEntities) {
        List<EquipmentItem> equipment = equipmentEntities.stream()
                .map(this::toEquipmentItem)
                .toList();

        return new VikingView(
                entity.id(),
                entity.name(),
                entity.age(),
                entity.heightCm(),
                entity.hairColor(),
                entity.beardStyle(),
                equipment
        );
    }

    public VikingView toVikingView(Integer id, Viking viking) {
        return new VikingView(
                id,
                viking.name(),
                viking.age(),
                viking.heightCm(),
                viking.hairColor(),
                viking.beardStyle(),
                viking.equipment()
        );
    }

    public Viking toViking(VikingView view) {
        return new Viking(
                view.name(),
                view.age(),
                view.heightCm(),
                view.hairColor(),
                view.beardStyle(),
                view.equipment()
        );
    }
}
