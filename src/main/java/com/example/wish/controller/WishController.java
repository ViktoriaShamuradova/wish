package com.example.wish.controller;

import com.example.wish.dto.SearchScreenWishDto;
import com.example.wish.dto.wish.*;
import com.example.wish.model.search_request.WishSearchRequest;
import com.example.wish.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/demo/wish")
@RequiredArgsConstructor
public class WishController {

    private final WishService wishService;

    /**
     * Возвращает собственные желания текущего юзера для главного экрана в статусе new,
     * in_progress
     * /own-main возможно так для урла будет лучше
     *
     * @return
     */
    @GetMapping("/own-main")
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
    @GetMapping("/executing-main")
    public ResponseEntity<List<AbstractWishDto>> getExecutingWishesMainScreen() {
        return ResponseEntity.ok(wishService.getExecutingWishesMainScreen());
    }

    /**
     * Возвращает изображение желания в любом статусе
     * Если изображения нет - возвращает пустой ответ со статусом 200
     *
     * @return
     */
    @GetMapping(value = "/image/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getImage(@PathVariable("id") long wishId) {
        return ResponseEntity.ok(wishService.findImage(wishId));
    }

    /**
     * используется для просмотра желания, свое и чужое. свле в любом статусе. чужое - новое, в процессе исполнения
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<AbstractWishDto> getWish(@PathVariable("id") long id) {
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

    //описание не может быть пустым, также заголовок, приоритет
    //приоритет, теги с больших букв и так как указано в enum
    @PostMapping(value = "/create")
    public ResponseEntity<WishDto> createWish(@RequestBody @Valid CreateWishRequest wishDto,
                                              UriComponentsBuilder uriBuilder) {
        WishDto wish = wishService.create(wishDto);

        UriComponents uriComponents = uriBuilder
                .path("/v1/demo/wish/own/{id}")
                .buildAndExpand(wish.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    //NumberFormatException - if wishId doesn't convert string wish id into long
    @PostMapping(value = "/upload-image")
    public ResponseEntity<Void> uploadImage(@RequestParam("file") @NotNull MultipartFile file,
                                            @RequestParam("wishId") @NotNull Long wishId,
                                            UriComponentsBuilder uriBuilder) throws IOException, HttpMediaTypeNotSupportedException {
        wishService.uploadImage(wishId, file);

        UriComponents uriComponents = uriBuilder
                .path("/v1/demo/wish/image/{id}")
                .buildAndExpand(wishId);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    /**
     * fields - содержат только измененные значения
     * если передать то же значение priority - будет исключение "You have a wish of the same priority",
     * если передать tagNames: null - "Error updating wish. Invalid value for tags field." В данном случае передать  "tagNames": []
     * если переданы поля, которые нельзя обновить, наприем, status - исключение "unable to update field= "
     *
     * @param id
     * @param fields
     * @return
     * @throws ParseException
     */

    @PatchMapping(value = "/{id}")
    public ResponseEntity<WishDto> updateWish(@PathVariable("id") long id,
                                              @RequestBody Map<String, Object> fields) {
        return ResponseEntity.ok(wishService.updateByFields(id, fields));
    }

    /**
     * В желании можно изменить название, описание, приоритет, теги
     * передается полностью желание со старыми значениями
     * если оглавление пустое - исключение
     * если описание пустое - исключение
     * если приоритет пустой - исключение
     * теги пустые - удаляются теги
     *
     * @param id
     * @param updateWishRequest
     * @return
     * @throws ParseException
     */
    @PostMapping(value = "/update/{id}")
    public ResponseEntity<WishDto> updateWishWithUpdateRequest(@PathVariable("id") long id,
                                                               @Valid @RequestBody UpdateWishRequest updateWishRequest) {
        return ResponseEntity.ok(wishService.update(id, updateWishRequest));
    }


    /**
     * когда взял на исполнение желание
     *
     * @param anonymously
     * @param id
     * @return
     */
    @GetMapping("/execute/{id}")
    public ResponseEntity<Void> executeWish(@RequestParam(name = "anonymously", required = false) boolean anonymously,
                                      @PathVariable("id") long id) {
        wishService.execute(id, anonymously);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Исполнитель отвправляет этот запрос, когда от исполнил желание.
     * В этом случае меняеся статус желания в исполнении на ожидающее подтверждение.
     *
     * @param id
     * @return
     */
    @GetMapping("/finish/{id}")
    public ResponseEntity<Void> finishWish(@PathVariable("id") long id) {
        wishService.finish(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * запрос отправляет собственник желания, когда его желание в ожидании подтверждения.
     * Он отправляет запрос с подтверждением или нет. соответствующий флаг в реквесте
     *
     * @param confirmWishRequest
     * @return
     */

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmWish(@RequestBody ConfirmWishRequest confirmWishRequest) {
        wishService.confirm(confirmWishRequest);
        return ResponseEntity.noContent().build();
    }

    /**
     * когда передумалы выполнять желание
     *
     * @param id
     * @return
     */
    @GetMapping("/cancelExecution/{id}")
    public ResponseEntity<Void> cancelExecutionWish(@PathVariable("id") long id) {
        wishService.cancelExecution(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteWish(@PathVariable("id") long id) {
        wishService.delete(id);
        return ResponseEntity.noContent().build();
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