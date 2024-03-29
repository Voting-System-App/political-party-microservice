package com.app.political.party.microservice.services.impl;

import com.app.political.party.microservice.entities.Candidate;
import com.app.political.party.microservice.repositories.CandidateRepository;
import com.app.political.party.microservice.services.CandidateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository candidateRepository;

    public CandidateServiceImpl(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<Candidate> findAll() {
        return candidateRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<Candidate> findByVotingId(String id) {
        return candidateRepository.findAllByVotingId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<Candidate> findByPoliticalParty(String id) {
        return candidateRepository.findByPoliticalParty_Id(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Candidate> findById(String id) {
        return candidateRepository.findById(id);
    }

    @Override
    @Transactional
    public Mono<Candidate> save(Candidate candidate) {
        candidate.getPoliticalParty().setStatus(false);
        candidate.setGender(Boolean.parseBoolean(String.valueOf(candidate.getGender())));
        return candidateRepository.save(candidate);
    }

    @Override
    @Transactional
    public Mono<Candidate> update(Candidate candidate, String id) {
        return candidateRepository.findById(id)
                .flatMap(result->{
                    result.setDni(candidate.getDni());
                    result.setEmail(candidate.getEmail());
                    result.setGender(Boolean.parseBoolean(String.valueOf(candidate.getGender())));
                    result.setName(candidate.getName());
                    result.setLastName(candidate.getLastName());
                    result.setBirthDate(candidate.getBirthDate());
                    result.setVotingId(candidate.getVotingId());
                    result.setPoliticalParty(candidate.getPoliticalParty());
                    return candidateRepository.save(result);
                });
    }

    @Override
    public Mono<String> delete(String id) {
        return candidateRepository.deleteById(id).thenReturn("id: "+id+" eliminado");
    }
}
