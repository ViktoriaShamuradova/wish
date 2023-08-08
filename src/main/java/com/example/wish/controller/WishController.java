package com.example.wish.controller;

import com.example.wish.dto.*;
import com.example.wish.dto.wish.ConfirmWishRequest;
import com.example.wish.dto.wish.CreateWishRequest;
import com.example.wish.dto.wish.SearchWishDto;
import com.example.wish.dto.wish.StoryWishDto;
import com.example.wish.model.search_request.WishSearchRequest;
import com.example.wish.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.List;
//проверить момент, что если желание какого-то приоритета в процессе, то создавать желание
//этого приоритета нельзя, потому что исполнитель может отменить выполнение желания, и желание переходит в статус new
// тогда получается, что два желания одного приоритета, что по правилам так нельзя

//if service return void  - need ResponseEntity.ok().build() <?>
@RestController
@RequestMapping("/v1/demo/wish")
@RequiredArgsConstructor
public class WishController {

    private final WishService wishService;

    /**
     * Возвращает собственные желания текущего юзера для главного экрана в статусе new,
     * in_progress
     * /own-main возможно так для урла будет лучше
     * @return
     */
    @GetMapping("/own")
    public ResponseEntity<List<AbstractWishDto>> getOwnWishesMainScreen() {
        return ResponseEntity.ok(wishService.getOwmWishesMainScreen());
    }

    /**
     * Возвращает другие желания, который текущий юзер исполняет  для главного экрана
     * пока только в статусе
     * статус in-progress, finish_failed - например возвращать определенное количество дней. Далее помещается в историю
     * in_progress
     *
     * @return
     */
    @GetMapping("/another-main")
    public ResponseEntity<List<AbstractWishDto>> getAnotherWishesMainScreen() {
        return ResponseEntity.ok(wishService.getAnotherWishesMainScreen());
    }

    /**
     * Возвращает собственные желания текущего юзера в любом статусе
     *
     * @return
     */
    @GetMapping("/own/{id}")
    public ResponseEntity<AbstractWishDto> getOwnWish(@PathVariable("id") long id) {
        return ResponseEntity.ok(wishService.getOwmWish(id));

       // return ResponseEntity.ok(wishService.getOwmWishesInProgress());
    }

    /**
     * отображать желание других пользоватедей в статусе new
     * @param id
     * @return
     */
    @GetMapping("/another/{id}")
    public ResponseEntity<AbstractWishDto> findWish(@PathVariable("id") long id) {
        return ResponseEntity.ok(wishService.find(id));
    }

    /**
     * используется, когда происходит фильтрация, отображаются желания для поиска только в статусе new
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

    @PostMapping(value = "/create")
    public ResponseEntity<MainScreenProfileDto> createWish(@RequestPart("dto") @Valid CreateWishRequest wishDto) throws ParseException, IOException {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/boomerang/v1/demo/wish/create").toUriString());

        MainScreenProfileDto mainScreenProfileDto = wishService.create(wishDto);
        return ResponseEntity.created(uri).body(mainScreenProfileDto);
    }

    @PutMapping(value = "/update/{id}")
    public ResponseEntity<MainScreenProfileDto> updateWish(@PathVariable("id") long id, @RequestPart("dto") @Valid CreateWishRequest wishDto) throws ParseException, IOException {

        MainScreenProfileDto mainScreenProfileDto = wishService.update(id, wishDto);
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
