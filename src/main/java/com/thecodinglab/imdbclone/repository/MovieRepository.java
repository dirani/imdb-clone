package com.thecodinglab.imdbclone.repository;

import com.thecodinglab.imdbclone.entity.Movie;
import com.thecodinglab.imdbclone.exception.NotFoundException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovieRepository extends JpaRepository<Movie, Long> {

  /** Can be used in combination with indexMovies/partition-method to index movies */
  List<Movie> findByImdbRatingCountBetween(Integer minRatingCount, Integer maxRatingCount);

  @Query("select m from Movie m where m.id in :movieIds")
  Page<Movie> findByIds(@Param("movieIds") List<Long> movieIds, Pageable pageable);

  default Movie getMovieById(Long movieId) {
    return findById(movieId)
        .orElseThrow(
            () -> new NotFoundException("Movie with id [" + movieId + "] not found in database."));
  }
}
