package com.example.wish.controller;

import com.example.wish.dto.*;
import com.example.wish.model.search_request.WishSearchRequest;
import com.example.wish.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
//if service return void  - need ResponseEntity.ok().build() <?>
@RestController
@RequestMapping("/boomerang/v1/demo/wish")
@RequiredArgsConstructor
public class WishController {

    private final WishService wishService;

    @PostMapping(value = "/create")
    public ResponseEntity<MainScreenProfileDto> createWish(@RequestPart("dto") @Valid CreateWishRequest wishDto, @RequestPart("image") MultipartFile file) throws ParseException, IOException {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/boomerang/v1/demo/wish/create").toUriString());

        MainScreenProfileDto mainScreenProfileDto = wishService.create(wishDto, file);
        return ResponseEntity.created(uri).body(mainScreenProfileDto);
        //return ResponseEntity.ok(mainScreenProfileDto);
    }

    @PutMapping(value = "/update/{id}")
    public ResponseEntity<MainScreenProfileDto> updateWish(@PathVariable("id") long id, @RequestPart("dto") @Valid CreateWishRequest wishDto, @RequestPart("image") MultipartFile file) throws ParseException, IOException {

        MainScreenProfileDto mainScreenProfileDto = wishService.update(id, wishDto, file);
        return ResponseEntity.ok(mainScreenProfileDto);
    }

    /**
     * когда взял на исполнение желание
     *
     * @param anonymously
     * @param id
     * @return
     */
    @GetMapping("/execute/{id}")
    public ResponseEntity<MainScreenProfileDto> executeWish(@RequestParam(name = "anonymously", required = false) boolean anonymously,
                                                            @PathVariable("id") long id) {
        MainScreenProfileDto profile = wishService.execute(id, anonymously);
        return ResponseEntity.ok(profile);
    }

    /**
     * Исполнитель отвправляет этот запрос, когда от исполнил желание.
     * В этом случае меняеся статус желания в исполнении на ожидающее подтверждение.
     *
     * @param id
     * @return
     */
    @GetMapping("/finish/{id}")
    public ResponseEntity<MainScreenProfileDto> finishWish(@PathVariable("id") long id) {
        MainScreenProfileDto profile = wishService.finish(id);
        return ResponseEntity.ok(profile);
    }

    /**
     * запрос отправляет собственник желания, когда его желание в ожидании подтверждения.
     * Он отправляет запрос с подтверждением или нет. соответствующий флаг в реквесте
     *
     * @param confirmWishRequest
     * @return
     */

    @PostMapping("/confirm")
    public ResponseEntity<MainScreenProfileDto> confirmWish(@RequestBody ConfirmWishRequest confirmWishRequest) {
        MainScreenProfileDto profile = wishService.confirm(confirmWishRequest);
        return ResponseEntity.ok(profile);
    }

    /**
     * когда передумалы выполнять желание
     *
     * @param id
     * @return
     */
    @GetMapping("/cancelExecution/{id}")
    public ResponseEntity<MainScreenProfileDto> cancelExecutionWish(@PathVariable("id") long id) {
        MainScreenProfileDto profile = wishService.cancelExecution(id);
        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<MainScreenProfileDto> deleteWish(@PathVariable("id") long id) {
        MainScreenProfileDto profile = wishService.delete(id);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AbstractWishDto> findWish(@PathVariable("id") long id) {
        return ResponseEntity.ok(wishService.find(id));
    }

    /**
     * используется, когда происходит фильтрация
     *
     * @param request
     * @param pageable
     * @return
     */
    @PostMapping(value = "/search")
    public Page<SearchWishDto> search(@RequestBody WishSearchRequest request,
                                      @SortDefault(sort = "priorityRank", direction = Sort.Direction.ASC) Pageable pageable) {
        return wishService.find(pageable, request);
    }

    /**
     * возвращает список всех тегов, количество желаний, желания в статусе new
     *
     * @param pageable
     * @return
     */
    @GetMapping("/findAll")
    public ResponseEntity<SearchScreenWishDto> findAll(@SortDefault(sort = "priorityRank", direction = Sort.Direction.ASC) Pageable pageable) {
        SearchScreenWishDto wishes = wishService.getSearchScreenWish(pageable);
        return ResponseEntity.ok(wishes);
    }

    @GetMapping("/story")
    public ResponseEntity<StoryWishDto> findStoryOfWish() {
        return ResponseEntity.ok(wishService.getStoryWish());
    }

}
