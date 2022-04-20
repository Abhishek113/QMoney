
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.PortfolioManagerApplication;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {


  private RestTemplate restTemplate;
  private StockQuotesService stockQuotesService;

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  protected PortfolioManagerImpl(StockQuotesService stockQuotesService)
  {
      this.stockQuotesService = stockQuotesService;
  }

  protected PortfolioManagerImpl(RestTemplate restTemplate, StockQuotesService stockQuotesService)
  {
      this.restTemplate = restTemplate;
      this.stockQuotesService = stockQuotesService;
  }


  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF

  public AnnualizedReturn getAnnualizedReturn(PortfolioTrade portfolioTrade, LocalDate endDate)
  {
    AnnualizedReturn annualizedReturn = new AnnualizedReturn("", 0.0, 0.0);

    String symbol = portfolioTrade.getSymbol();
    LocalDate startDate = portfolioTrade.getPurchaseDate();

    try{
      List<Candle> tiingoCandles = getStockQuote(symbol, startDate, endDate);
      Double openingPrice = PortfolioManagerApplication.getOpeningPriceOnStartDate(tiingoCandles);
      Double closingPrice = PortfolioManagerApplication.getClosingPriceOnEndDate(tiingoCandles);

      annualizedReturn = PortfolioManagerApplication.calculateAnnualizedReturns(endDate, portfolioTrade, openingPrice, closingPrice);

    }catch (JsonProcessingException e){
        System.out.println(e.getMessage());
        annualizedReturn = new AnnualizedReturn(symbol, Double.NaN, Double.NaN);
    }
    

    return annualizedReturn;
  }
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> pfTrades, LocalDate endDate) {
      
    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
    
    String token = PortfolioManagerApplication.getToken();

    for(PortfolioTrade portfolioTrade: pfTrades)
    {
      AnnualizedReturn annualizedReturn = getAnnualizedReturn(portfolioTrade, endDate);
      annualizedReturns.add(annualizedReturn);
    }
    
    //annualizedReturns = PortfolioManagerApplication.getAnnualizedReturnList(pfTrades, endDate, token);
    Collections.sort(annualizedReturns);
    return annualizedReturns;
  }


  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, RuntimeException {

        return stockQuotesService.getStockQuote(symbol, from, to);
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
      String uriTemplate = "https://api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
            + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
      
      String token = PortfolioManagerApplication.getToken();
      String url = uriTemplate.replace("$APIKEY", token).replace("$SYMBOL", symbol)
                              .replace("$STARTDATE", startDate.toString())
                              .replace("$ENDDATE", endDate.toString());
      return url;
  }


  // ¶TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Modify the function #getStockQuote and start delegating to calls to
  //  stockQuoteService provided via newly added constructor of the class.
  //  You also have a liberty to completely get rid of that function itself, however, make sure
  //  that you do not delete the #getStockQuote function.

}
