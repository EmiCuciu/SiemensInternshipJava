package com.siemens.internship;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class ItemService {
//    @Autowired
//    private ItemRepository itemRepository;
//    private static ExecutorService executor = Executors.newFixedThreadPool(10);
//    private List<Item> processedItems = new ArrayList<>();
//    private int processedCount = 0;

    private ItemRepository itemRepository;
    private static ExecutorService executor;

    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {
        // 1. Colectare ID-uri - operatie sincronă
        List<Long> itemIds = itemRepository.findAllIds();

        // 2. Listă thread-safe pentru rezultate
        List<Item> processedItems = Collections.synchronizedList(new ArrayList<>());

        // 3. Creare task-uri paralele pentru fiecare item
        List<CompletableFuture<Void>> futures = itemIds.stream()
                .map(id -> CompletableFuture.runAsync(() -> {
                    try {
                        // 4. Procesare individuală (retrieve + update + save)
                        Item item = itemRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Item not found: " + id));

                        Thread.sleep(100); // Simulare procesare

                        item.setStatus("PROCESSED");
                        Item savedItem = itemRepository.save(item);
                        processedItems.add(savedItem);

                    } catch (InterruptedException e) {
                        // 5. Gestionare corectă a întreruperilor
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Processing interrupted for ID: " + id, e);
                    }
                }, executor))
                .toList();

        // 6. Așteptare finalizare TOATE operațiile
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> processedItems) // 7. Returnare rezultate finale
                .exceptionally(ex -> { // 8. Propagare erori
                    throw new RuntimeException("Batch processing failed: " + ex.getMessage(), ex);
                });
    }



    @PreDestroy
    public void cleanup() {
        executor.shutdownNow();
    }
}

