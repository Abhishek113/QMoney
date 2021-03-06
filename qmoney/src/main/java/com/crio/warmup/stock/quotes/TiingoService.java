
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;


import org.springframework.web.client.RestTemplate;
import java.util.Collections;

public class TiingoService implements StockQuotesService {

  private RestTemplate restTemplate;

  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  protected static String getToken() {
    return "088fb67c203dc236a124f65dfff1e513a6285658";
  }


  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement getStockQuote method below that was also declared in the interface.

  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
  // 2. Run the tests using command below and make sure it passes.
  //    ./gradlew test --tests TiingoServiceTest


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) 
  throws JsonProcessingException, RuntimeException, StockQuoteServiceException {

    if(from.isAfter(to) || from.equals(to))
    {
      throw new RuntimeException();
    }
    // String url = buildTiingoUrl(symbol, from, to);
    // RestTemplate restTemplate = new RestTemplate();

    // TiingoCandle[] tiingoCandlesArray = restTemplate.getForObject(url, TiingoCandle[].class);

    // if(tiingoCandlesArray != null)
    // {
    //   Arrays.sort(tiingoCandlesArray);
    //   return Arrays.asList(tiingoCandlesArray);
    // }
      

    // return Collections.emptyList();

    String url = buildTiingoUrl(symbol,from,to);
    try{
        String response = restTemplate.getForObject(url, String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        Candle[] obj = objectMapper.readValue(response, TiingoCandle[].class);

        if(response == null || obj == null)
            throw new StockQuoteServiceException("");
        // if(obj==null) return new ArrayList<>();
        // else return Arrays.asList(obj);
        if(obj != null) return Arrays.asList(obj);
    }catch(StockQuoteServiceException e)
    {
        throw new StockQuoteServiceException("");
    }
    catch(RuntimeException e)
    {
        throw new RuntimeException("");
    }

    return new ArrayList<>();
  }


  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Write a method to create appropriate url to call the Tiingo API.

  protected String buildTiingoUrl(String symbol, LocalDate startDate, LocalDate endDate) {
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
          + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
    
    String token = TiingoService.getToken();
    String url = uriTemplate.replace("$APIKEY", token).replace("$SYMBOL", symbol)
                            .replace("$STARTDATE", startDate.toString())
                            .replace("$ENDDATE", endDate.toString());
    return url; 
  } 

}
