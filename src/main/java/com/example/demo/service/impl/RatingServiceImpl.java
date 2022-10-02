package com.example.demo.service.impl;

import com.example.demo.Payload.MessageResponse;
import com.example.demo.Payload.PagedResponse;
import com.example.demo.Payload.RatingRecord;
import com.example.demo.Payload.mapper.CustomRatingMapper;
import com.example.demo.entity.Account;
import com.example.demo.entity.Movie;
import com.example.demo.entity.Rating;
import com.example.demo.entity.RatingId;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.RatingRepository;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.RatingService;
import com.example.demo.util.Pagination;
import java.math.BigDecimal;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class RatingServiceImpl implements RatingService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RatingServiceImpl.class);

  private final MovieRepository movieRepository;
  private final AccountRepository accountRepository;
  private final RatingRepository ratingRepository;
  private final CustomRatingMapper ratingMapper;

  public RatingServiceImpl(
      MovieRepository movieRepository,
      AccountRepository accountRepository,
      RatingRepository ratingRepository,
      CustomRatingMapper ratingMapper) {
    this.movieRepository = movieRepository;
    this.accountRepository = accountRepository;
    this.ratingRepository = ratingRepository;
    this.ratingMapper = ratingMapper;
  }

  @Override
  public Rating rateMovie(UserPrincipal currentAccount, Long movieId, BigDecimal score) {
    if (score.floatValue() < 0 || score.floatValue() > 10.1) {
      throw new BadRequestException("Score must be between 0 and 10");
    } else {
      Movie movie = movieRepository.getMovieById(movieId);
      Account account = accountRepository.getAccount(currentAccount);
      Rating rating =
          new Rating(score, movie, account, new RatingId(movie.getId(), account.getId()));
      Rating savedRating = ratingRepository.save(rating);
      LOGGER.info("rating with id [{}] was created.", savedRating.getId());
      return savedRating;
    }
  }

  @Override
  public PagedResponse<RatingRecord> getRatingsByAccount(String username, int page, int size) {
    Pagination.validatePageNumberAndSize(page, size);
    Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAtInUtc");
    Account account = accountRepository.getAccountByName(username);
    Page<Rating> ratings = ratingRepository.findRatingsByAccount(account, pageable);
    Page<RatingRecord> ratingRecordPage = ratings.map(ratingMapper::entityToDTO);
    if (ratings.getContent().isEmpty()) {
      throw new NotFoundException(
          "ratings of account with id [" + account.getId() + "] not found in database.");
    }
    LOGGER.info(
        "[{}] ratings from account with id [{}] were retrieved.",
        ratings.getContent().size(),
        account.getId());
    return new PagedResponse<>(
        ratingRecordPage.getContent(),
        ratingRecordPage.getNumber(),
        ratingRecordPage.getSize(),
        ratingRecordPage.getTotalElements(),
        ratingRecordPage.getTotalPages(),
        ratingRecordPage.isLast());
  }

  @Override
  public MessageResponse deleteRating(UserPrincipal currentAccount, Long movieId) {
    Rating rating =
        ratingRepository
            .findRatingByAccountIdAndMovieId(currentAccount.getId(), movieId)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Rating with movieId ["
                            + movieId
                            + "] and accountId ["
                            + currentAccount.getId()
                            + "] not found in database."));
    if (Objects.equals(rating.getAccount().getId(), currentAccount.getId())
        || UserPrincipal.isCurrentAccountAdmin(currentAccount)) {
      ratingRepository.delete(rating);
      LOGGER.info("rating with id [{}] was deleted.", rating.getId());
      return new MessageResponse(
          "WatchedMovie with movieId ["
              + movieId
              + "] and accountId ["
              + currentAccount.getId()
              + "] was deleted");
    } else {
      throw new UnauthorizedException(
          "Account with id ["
              + currentAccount.getId()
              + "] has no permission to delete this resource.");
    }
  }
}
