package com.siemens.internship;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @Test
    void shouldProcessAllItems() throws Exception {
        // Given
        Item item1 = new Item(1L, "Item1", "Desc1", "NEW", "test1@example.com");
        Item item2 = new Item(2L, "Item2", "Desc2", "NEW", "test2@example.com");

        when(itemRepository.findAllIds()).thenReturn(List.of(1L, 2L));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));

        // Mock save() să returneze itemul actualizat
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            item.setStatus("PROCESSED");
            return item;
        });

        // When
        CompletableFuture<List<Item>> result = itemService.processItemsAsync();
        List<Item> processedItems = result.get();

        // Then
        assertEquals(2, processedItems.size());
        assertNotNull(processedItems.get(0), "Item 1 nu ar trebui să fie null");
        assertNotNull(processedItems.get(1), "Item 2 nu ar trebui să fie null");
        assertEquals("PROCESSED", processedItems.get(0).getStatus());
        assertEquals("PROCESSED", processedItems.get(1).getStatus());
    }

    @Test
    void shouldReturnEmptyListWhenNoItems() throws Exception {
        // Given
        when(itemRepository.findAllIds()).thenReturn(List.of());

        // When
        CompletableFuture<List<Item>> result = itemService.processItemsAsync();
        List<Item> processedItems = result.get();

        // Then
        assertTrue(processedItems.isEmpty());
    }

    @Test
    void shouldFindAllItems() {
        // Given
        Item item = new Item(1L, "Item1", "Desc1", "NEW", "test@example.com");
        when(itemRepository.findAll()).thenReturn(List.of(item));

        // When
        List<Item> items = itemService.findAll();

        // Then
        assertEquals(1, items.size());
        assertEquals(item, items.get(0));
    }

    @Test
    void shouldFindItemById() {
        // Given
        Item item = new Item(1L, "Item1", "Desc1", "NEW", "test@example.com");
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // When
        Optional<Item> foundItem = itemService.findById(1L);

        // Then
        assertTrue(foundItem.isPresent());
        assertEquals(item, foundItem.get());
    }
}