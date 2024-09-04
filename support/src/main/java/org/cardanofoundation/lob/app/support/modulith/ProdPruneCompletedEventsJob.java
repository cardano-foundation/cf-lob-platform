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
//@Profile({ "prod", "sandbox" })
//public class ProdPruneCompletedEventsJob {
//
//    private final EventPublicationRepository eventPublicationRepository;
//
//    @PostConstruct
//    public void init() {
//        log.info("Starting ProdPruneCompletedEventsJob...");
//    }
//
//    @Scheduled(cron = "${lob.spring.modulith.completed-events-pruning-cron:0 0 0 * * *}")
//    public void execute() {
//        log.info("Pruning completed events...");
//
//        eventPublicationRepository.deleteCompletedPublications();
//
//        log.info("Completed events pruned.");
//    }
//
//}
