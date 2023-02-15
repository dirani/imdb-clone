package com.thecodinglab.imdbclone.controller;

import com.thecodinglab.imdbclone.entity.Movie;
import com.thecodinglab.imdbclone.payload.PagedResponse;
import com.thecodinglab.imdbclone.payload.movie.MovieSearchRequest;
import com.thecodinglab.imdbclone.service.ElasticSearchService;
import com.thecodinglab.imdbclone.validation.Pagination;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping(("/api/search"))
public class SearchController {

  private final ElasticSearchService elasticSearchService;

  public SearchController(ElasticSearchService elasticSearchService) {
    this.elasticSearchService = elasticSearchService;
  }

  @GetMapping("/movies")
  public ResponseEntity<PagedResponse<Movie>> search(
      @RequestBody MovieSearchRequest request,
      @RequestParam(defaultValue = Pagination.DEFAULT_PAGE_NUMBER) int page,
      @RequestParam(defaultValue = Pagination.DEFAULT_PAGE_SIZE) int size) {
    return new ResponseEntity<>(
        elasticSearchService.searchMovies(request, page, size), HttpStatus.OK);
  }
}
