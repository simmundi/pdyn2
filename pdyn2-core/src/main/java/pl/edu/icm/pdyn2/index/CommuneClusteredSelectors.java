package pl.edu.icm.pdyn2.index;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.HouseholdMapper;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.ecs.selector.Selector;
import pl.edu.icm.trurl.ecs.util.ManuallyChunkedArraySelector;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.store.attribute.EntityListAttribute;
import pl.edu.icm.trurl.util.Status;

import java.util.Collection;
import java.util.stream.Stream;

public class CommuneClusteredSelectors {
    private static final int ESTIMATED_CHUNK_SIZE = 15_000;
    private final EngineConfiguration engineConfiguration;
    private final CommuneManager communeManager;
    private ManuallyChunkedArraySelector personSelector;
    private ManuallyChunkedArraySelector householdSelector;

    @WithFactory
    public CommuneClusteredSelectors(EngineConfiguration engineConfiguration, Selectors selectors, CommuneManager communeManager) {
        this.engineConfiguration = engineConfiguration;
        this.communeManager = communeManager;
        engineConfiguration.addComponentClasses(Person.class, Household.class, Location.class);
    }

    public synchronized Selector personInTerytSelector(Collection<String> teryts) {
        if (personSelector == null) {
            createSelectors();
        }
        return new Selector() {
            @Override
            public Stream<Chunk> chunks() {
                return personSelector.chunks().filter(chunk -> teryts.stream().anyMatch(t -> chunk.getChunkInfo().getLabel().startsWith(t)));
            }
            @Override
            public int estimatedChunkSize() {
                return ESTIMATED_CHUNK_SIZE;
            }
        };
    }

    private void createSelectors() {
        HouseholdMapper householdMapper = (HouseholdMapper) engineConfiguration.getEngine().getMapperSet().classToMapper(Household.class);
        Mapper<Location> locationMapper = engineConfiguration.getEngine().getMapperSet().classToMapper(Location.class);
        EntityListAttribute members = engineConfiguration.getEngine().getStore().get("members");

        Multimap<String, Integer> multimap = HashMultimap.create(2500, 6000);

        {
            Status status = Status.of("locating households", 1000_000);
            for (int id = 0; id < engineConfiguration.getEngine().getCount(); id++) {
                if (!householdMapper.isPresent(id)) {
                    continue;
                }
                status.tick();
                multimap.put(communeManager.communeAt(KilometerGridCell.fromLocation(locationMapper.createAndLoad(id))).getTeryt(),
                        id);
            }
            status.done();
        }

        personSelector = new ManuallyChunkedArraySelector(39_000_000, multimap.keySet().size());
        householdSelector = new ManuallyChunkedArraySelector(15_000_000, multimap.keySet().size());
        {
            Status status = Status.of("indexing households and people", 1000_000);

            multimap.asMap().forEach((teryt, ids) -> {
                for (var id : ids) {
                    householdSelector.add(id);
                    members.loadIds(id, ((index, value) -> personSelector.add(value)));
                    status.tick();
                }
                householdSelector.endChunk(teryt);
                personSelector.endChunk(teryt);
            });

            status.done();
        }
    }
}
