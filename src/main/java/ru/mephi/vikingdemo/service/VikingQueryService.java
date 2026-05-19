package ru.mephi.vikingdemo.service;

import org.springframework.stereotype.Service;
import ru.mephi.vikingdemo.model.BeardStyle;
import ru.mephi.vikingdemo.model.EquipmentItem;
import ru.mephi.vikingdemo.model.HairColor;
import ru.mephi.vikingdemo.model.VikingView;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

@Service
public class VikingQueryService {

    private final VikingService vikingService;
    private final Random chooser = new Random();

    public VikingQueryService(VikingService vikingService) {
        this.vikingService = vikingService;
    }

    public long countAgeGreaterThan(int age) {
        return countMatches(view -> view.age() > age);
    }

    public long countAgeLessThan(int age) {
        return countMatches(view -> view.age() < age);
    }

    public long countAgeInsideRange(int min, int max) {
        return countMatches(view -> view.age() >= min && view.age() <= max);
    }

    public long countAgeOutsideRange(int min, int max) {
        return countMatches(view -> view.age() < min || view.age() > max);
    }

    public long countByAppearance(BeardStyle beardStyle, HairColor hairColor) {
        return countMatches(view -> view.beardStyle() == beardStyle && view.hairColor() == hairColor);
    }

    public long countWithAxes(int expectedCount) {
        return countMatches(view -> axesAmount(view.equipment()) == expectedCount);
    }

    public Optional<VikingView> pickRandomTallViking() {
        List<VikingView> tallVikings = vikingService.findAll().stream()
                .filter(view -> view.heightCm() > 180)
                .toList();

        if (tallVikings.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(tallVikings.get(chooser.nextInt(tallVikings.size())));
    }

    public List<VikingView> listLegendaryOwners() {
        return vikingService.findAll().stream()
                .filter(view -> equipmentOf(view).stream().anyMatch(this::legendary))
                .toList();
    }

    public List<VikingView> listRedVikingsByAge() {
        return vikingService.findAll().stream()
                .filter(view -> view.hairColor() == HairColor.Red)
                .sorted(Comparator.comparingInt(VikingView::age))
                .toList();
    }

    public Optional<Integer> findMaxId(Integer[] ids) {
        return Arrays.stream(ids)
                .filter(Objects::nonNull)
                .max(Integer::compare);
    }

    public List<Integer> collectEvenIds(Integer[] ids) {
        return Arrays.stream(ids)
                .filter(Objects::nonNull)
                .filter(id -> id % 2 == 0)
                .toList();
    }

    public Integer[] currentIds() {
        return vikingService.findAll().stream()
                .map(VikingView::id)
                .filter(Objects::nonNull)
                .toArray(Integer[]::new);
    }

    public List<VikingView> createRandomPack(int amount) {
        return vikingService.createRandomPack(amount);
    }

    private long countMatches(Predicate<VikingView> rule) {
        return vikingService.findAll().stream()
                .filter(rule)
                .count();
    }

    private long axesAmount(List<EquipmentItem> equipment) {
        if (equipment == null) {
            return 0;
        }

        return equipment.stream()
                .filter(item -> item.name() != null)
                .filter(item -> item.name().equalsIgnoreCase("Axe"))
                .count();
    }

    private List<EquipmentItem> equipmentOf(VikingView view) {
        if (view.equipment() == null) {
            return List.of();
        }

        return view.equipment();
    }

    private boolean legendary(EquipmentItem item) {
        return item.quality() != null && item.quality().equalsIgnoreCase("Legendary");
    }
}
