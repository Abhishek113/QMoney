
package com.crio.warmup.stock.quotes;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AlphavantageDailyResponse;
import com.crio.warmup.stock.dto.AlphavantageCandle;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

public class AlphavantageService implements StockQuotesService {

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement the StockQuoteService interface as per the contracts. Call Alphavantage service
  //  to fetch daily adjusted data for last 20 years.
  //  Refer to documentation here: https://www.alphavantage.co/documentation/
  //  --
  //  The implementation of this functions will be doing following tasks:
  //    1. Build the appropriate url to communicate with third-party.
  //       The url should consider startDate and endDate if it is supported by the provider.
  //    2. Perform third-party communication with the url prepared in step#1
  //    3. Map the response and convert the same to List<Candle>
  //    4. If the provider does not support startDate and endDate, then the implementation
  //       should also filter the dates based on startDate and endDate. Make sure that
  //       result contains the records for for startDate and endDate after filtering.
  //    5. Return a sorted List<Candle> sorted ascending based on Candle#getDate
  //  IMP: Do remember to write readable and maintainable code, There will be few functions like
  //    Checking if given date falls within provided date range, etc.
  //    Make sure that you write Unit tests for all such functions.
  //  Note:
  //  1. Make sure you use {RestTemplate#getForObject(URI, String)} else the test will fail.
  //  2. Run the tests using command below and make sure it passes:
  //    ./gradlew test --tests AlphavantageServiceTest
  //CHECKSTYLE:OFF

  private RestTemplate restTemplate;

  public AlphavantageService(RestTemplate restTemplate)
  {
    this.restTemplate = restTemplate;
  }

  protected static String getAPIKey()
  {
      return "T9EQSZ5QVL4GWGX8";
  }

  private static Boolean isValidDate(LocalDate currDate, LocalDate from, LocalDate to)
  {
      if((currDate.equals(from) || currDate.isAfter(from)) && (currDate.equals(to) || currDate.isBefore(to)))
      {
          return true;
      }

      return false;
  }
  private static List<Candle> getAlphaventageCandleInStartAndEndDate(HashMap<LocalDate, AlphavantageCandle> alphavantageCandlesMap, LocalDate from, LocalDate to)
  {
      List<Candle> alphavaCandles = new ArrayList<>();
      if(alphavantageCandlesMap != null)
      {
        for(HashMap.Entry<LocalDate, AlphavantageCandle> candle: alphavantageCandlesMap.entrySet())
        {
            LocalDate currCandleDate = candle.getKey();
            
            if(isValidDate(currCandleDate, from, to))
            {
                AlphavantageCandle currAlphavantageCandle = candle.getValue();
                if(currAlphavantageCandle != null)
                {
                  currAlphavantageCandle.setDate(currCandleDate);
                  alphavaCandles.add(currAlphavantageCandle);
                }
            }
        }
      }
      
      return alphavaCandles;
  }


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonProcessingException, RuntimeException, StockQuoteServiceException
  {
      // if(from.isAfter(to) || from.equals(to))
      // {
      //   throw new RuntimeException();
      // }
      // String url = buildAlphavantageUrl(symbol);
      
      // RestTemplate restTemplate = new RestTemplate();

      // AlphavantageDailyResponse alphavantageDailyResponse = restTemplate.getForObject(url, 
      //                                                           AlphavantageDailyResponse.class);
                                                                
      // Map<LocalDate, AlphavantageCandle> alphavantageCandlesMap = alphavantageDailyResponse.getCandles();
      
      // List<Candle> alphavantageCandles = getAlphaventageCandleInStartAndEndDate(alphavantageCandlesMap, from, to);
      
      // if(alphavantageCandles.size() > 0)
      //   Collections.reverse(alphavantageCandles);

      // return alphavantageCandles;
      String url = buildAlphavantageUrl(symbol);
      try{

          String apiResponse = restTemplate.getForObject(url, String.class);
          System.out.println(apiResponse);
          ObjectMapper objectMapper = new ObjectMapper();
          objectMapper.registerModule(new JavaTimeModule());
          HashMap<LocalDate, AlphavantageCandle> alphavantageCandlesMap = (HashMap<LocalDate, AlphavantageCandle>) objectMapper
              .readValue(apiResponse, AlphavantageDailyResponse.class).getCandles();
          // if(obj==null) return new ArrayList<>();
          // List<Candle> res = new ArrayList<>();
          // for(LocalDate date = from;!date.isAfter(to);date = date.plusDays(1)){
          //   AlphavantageCandle candle = obj.get(date);
          //   if(candle!=null){
          //     candle.setDate(date);
          //     res.add(candle);
          //   }
          // }
          // return res;
          if(apiResponse == null || alphavantageCandlesMap == null)
            throw new StockQuoteServiceException("");
          List<Candle> alphavantageCandles = getAlphaventageCandleInStartAndEndDate(alphavantageCandlesMap, from, to);
          
          if(alphavantageCandles.size() > 0)
            Collections.reverse(alphavantageCandles);

          return alphavantageCandles;

      }catch (StockQuoteServiceException e)
      {
          throw new StockQuoteServiceException("");
      }catch (RuntimeException e)
      {
          throw new RuntimeException("");
      }
  }
    //CHECKSTYLE:ON
  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  1. Write a method to create appropriate url to call Alphavantage service. The method should
  //     be using configurations provided in the {@link @application.properties}.
  //  2. Use this method in #getStockQuote.


  protected static String buildAlphavantageUrl(String symbol)
  {
      String urlTemplate = "https://www.alphavantage.co/query?"
      + "function=TIME_SERIES_DAILY&symbol=$SYMBOL&outputsize=full&apikey=$APIKEY";

      String apiKey = AlphavantageService.getAPIKey();
      String url = urlTemplate.replace("$SYMBOL", symbol).replace("$APIKEY", apiKey);


      return url;

  } 
}

