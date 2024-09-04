//package org.cardanofoundation.lob.app.support.modulith;
//
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Profile;
//import org.springframework.modulith.events.core.EventPublicationRepository;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//@Profile({ "dev--yaci-dev-kit" })
//public class DevPruneCompletedEventsJob {
//
//    private final EventPublicationRepository eventPublicationRepository;
//
//    @PostConstruct
//    public void init() {
//        log.info("Starting DevPruneCompletedEventsJob...");
//    }
//
//    @Scheduled(fixedRateString = "${lob.spring.modulith.completed-events-pruning-rate:PT5M}")
//    public void execute() {
//        log.info("Pruning completed events...");
//
//        eventPublicationRepository.deleteCompletedPublications();
//
//        log.info("Completed events pruned.");
//    }
//
//}
