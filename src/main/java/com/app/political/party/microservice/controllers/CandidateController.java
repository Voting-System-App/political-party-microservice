package com.app.political.party.microservice.controllers;

import com.app.political.party.microservice.entities.Candidate;
import com.app.political.party.microservice.services.CandidateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Tag(name = "Candidate Api", description = "Api for testing the endpoint for candidate")
@RequestMapping("/candidate")
public class CandidateController {
    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @GetMapping
    public ResponseEntity<Flux<Candidate>> findAll(){
        Flux<Candidate> total = candidateService.findAll();
        return ResponseEntity.ok(total);
    }
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Candidate>> findById(@PathVariable String id){
        return candidateService.findById(id).map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @GetMapping("/voting/{id}")
    public ResponseEntity<Flux<Candidate>> findByVotingId(@PathVariable String id){
        Flux<Candidate> list = candidateService.findByVotingId(id);
        return ResponseEntity.ok(list);
    }
    @GetMapping("/party/{id}")
    public ResponseEntity<Flux<Candidate>> findByPoliticalParty(@PathVariable String id){
        Flux<Candidate> list = candidateService.findByPoliticalParty(id);
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<Mono<Candidate>> save(@RequestBody Candidate candidate){
        return ResponseEntity.ok(candidateService.save(candidate));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Candidate>> update(@RequestBody Candidate candidate,@PathVariable String id){
        return candidateService.update(candidate,id).map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<String>> delete(@PathVariable String id){
        return candidateService.delete(id).map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
