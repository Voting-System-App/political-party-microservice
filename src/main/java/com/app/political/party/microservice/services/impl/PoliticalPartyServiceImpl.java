package com.app.political.party.microservice.services.impl;

import com.app.political.party.microservice.entities.Adherent;
import com.app.political.party.microservice.entities.PoliticalParty;
import com.app.political.party.microservice.repositories.AdherentRepository;
import com.app.political.party.microservice.repositories.PoliticalPartyRepository;
import com.app.political.party.microservice.services.PoliticalPartyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class PoliticalPartyServiceImpl implements PoliticalPartyService {

    private final PoliticalPartyRepository partyRepository;

    private final AdherentRepository adherentRepository;
    private final String tempDirectory;
    public PoliticalPartyServiceImpl(PoliticalPartyRepository partyRepository, AdherentRepository adherentRepository, @Value("${adherent.directory}") String tempDirectory) {
        this.partyRepository = partyRepository;
        this.adherentRepository = adherentRepository;
        this.tempDirectory = tempDirectory;
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<PoliticalParty> findAll() {
        return partyRepository.findAll();
    }

    @Override
    public Flux<Adherent> findAllAdherents(String id) {
        return adherentRepository.findAllByPoliticalParty_Id(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<PoliticalParty> findById(String id) {
        return partyRepository.findById(id);
    }
    private Mono<Boolean> saveAdherentFromFile(PoliticalParty party, String path)throws IOException {
        String line = "";
        BufferedReader file = new BufferedReader(new FileReader(tempDirectory+path));
        List<Adherent> adherentList = new ArrayList<>();
        while ((line= file.readLine())!=null){
            String[] data = line.split(";",-1);
            Adherent adherent = new Adherent();
            adherent.setName(data[0]);
            adherent.setLastName(data[1]);
            adherent.setPosition(data[2]);
            adherent.setStatus(!data[2].isEmpty());
            adherent.setPoliticalParty(party);
            adherentList.add(adherent);
        }
        Boolean status = adherentList.stream().anyMatch(adherent -> !adherent.getStatus());
        adherentList.forEach(adherent->adherent.setStatus(status));
        file.close();
        return adherentRepository.saveAll(adherentList).then(Mono.just(status));
    }
    @Override
    @Transactional
    public Mono<PoliticalParty> save(PoliticalParty politicalParty){
        politicalParty.setStatus(false);
        return partyRepository.save(politicalParty);
    }

    @Override
    public Mono<PoliticalParty> update(PoliticalParty politicalParty, String id) {
        return partyRepository.findById(id).flatMap(result->{
            result.setName(politicalParty.getName());
            result.setDescription(politicalParty.getDescription());
            result.setCreationDate(politicalParty.getCreationDate());
            return partyRepository.save(result);
        });
    }

    @Override
    @Transactional
    public Mono<PoliticalParty> updateAdherentStatus(String id,String path) {
        Flux<Adherent> adherentFlux = adherentRepository.findAllByPoliticalParty_Id(id);
        Mono<PoliticalParty> response = partyRepository.findById(id).flatMap(result->{
            try {
                return saveAdherentFromFile(result,path).flatMap(status->{
                            result.setStatus(!status);
                            return partyRepository.save(result);
                        }).then(deleteFileAdherent(path)).thenReturn(result);
            } catch (IOException e) {
                return Mono.error(new RuntimeException(e));
            }
        });
        return adherentFlux.hasElements().flatMap(result->result?adherentRepository.deleteAllByPoliticalParty_Id(id).then(response):response);
    }

    @Override
    public Mono<String> delete(String id) {
        return partyRepository.deleteById(id).thenReturn("id: "+id+" eliminado");
    }
    private Mono<Boolean> deleteFileAdherent(String path) throws IOException {
        Path result = Path.of(tempDirectory+path);
        return Mono.just(Files.deleteIfExists(result));
    }
}
