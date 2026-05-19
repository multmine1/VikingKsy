package ru.mephi.vikingdemo.service;

import org.springframework.stereotype.Service;
import ru.mephi.vikingdemo.model.Viking;
import ru.mephi.vikingdemo.model.VikingView;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import ru.mephi.vikingdemo.repository.VikingStorage;

@Service
public class VikingService {
    // каждый раз при изменении создаётся новая копия списка 

    private final VikingFactory vikingFactory;
    private final VikingStorage vikingStorage;
    
    
    @Autowired
    public VikingService(
            VikingFactory vikingFactory,
            VikingStorage vikingStorage
    ) {
        this.vikingFactory = vikingFactory;
        this.vikingStorage = vikingStorage;
    }
    
    public List<VikingView> findAll() {
        return vikingStorage.findAll();
    }

    public VikingView createRandomViking() {
        Viking viking = vikingFactory.createRandomViking();
        return vikingStorage.save(viking);
    }

    public VikingView addViking(Viking viking) {
        return vikingStorage.save(viking);
    }

    public Optional<VikingView> getViking(int id) {
        return vikingStorage.findById(id);
    }

    public boolean deleteViking(int id) {
        return vikingStorage.deleteById(id);
    }

    public Optional<VikingView> updateViking(int id, Viking viking) {
        return vikingStorage.updateById(id, viking);
    }

    public List<VikingView> createRandomPack(int amount) {
        if (amount <= 0) {
            return List.of();
        }

        return IntStream.rangeClosed(1, amount)
                .mapToObj(number -> vikingFactory.createRandomViking())
                .map(vikingStorage::save)
                .toList();
    }
}
