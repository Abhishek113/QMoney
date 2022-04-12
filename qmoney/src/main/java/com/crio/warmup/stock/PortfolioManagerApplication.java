
package com.crio.warmup.stock;

import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
// import java.lang.reflect.Array;
import java.net.URISyntaxException;
// import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
// import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
// import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.management.RuntimeErrorException;

// import java.util.stream.Collectors;
// import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

  // TODO: CRIO_TASK_MODULE_REST_API
  //  Find out the closing price of each stock on the end_date and return the list
  //  of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  //    and deserialize the results in List<Candle>



  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
  
    // String filePath = "src/main/resources/" + args[0];
    //String filePath = args[0];
    File portfolioTtrades = resolveFileFromResources(args[0]);
    ObjectMapper objMpr = getObjectMapper();
    List<String> tradeSymbols = new ArrayList<>();
    try
    {
      PortfolioTrade[] pfTrades = objMpr.readValue(portfolioTtrades, PortfolioTrade[].class);

      for(PortfolioTrade pfTarde:pfTrades)
      {
        tradeSymbols.add(pfTarde.getSymbol());
      }
    }catch(Exception e)
    {
      System.out.println(e.getMessage());
    }

    return tradeSymbols;
}

  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  //  Follow the instructions provided in the task documentation and fill up the correct values for
  //  the variables provided. First value is provided for your reference.
  //  A. Put a breakpoint on the first line inside mainReadFile() which says
  //    return Collections.emptyList();
  //  B. Then Debug the test #mainReadFile provided in PortfoliomanagerApplicationTest.java
  //  following the instructions to run the test.
  //  Once you are able to run the test, perform following tasks and record the output as a
  //  String in the function below.
  //  Use this link to see how to evaluate expressions -
  //  https://code.visualstudio.com/docs/editor/debugging#_data-inspection
  //  1. evaluate the value of "args[0]" and set the value
  //     to the variable named valueOfArgument0 (This is implemented for your reference.)
  //  2. In the same window, evaluate the value of expression below and set it
  //  to resultOfResolveFilePathArgs0
  //     expression ==> resolveFileFromResources(args[0])
  //  3. In the same window, evaluate the value of expression below and set it
  //  to toStringOfObjectMapper.
  //  You might see some garbage numbers in the output. Dont worry, its expected.
  //    expression ==> getObjectMapper().toString()
  //  4. Now Go to the debug window and open stack trace. Put the name of the function you see at
  //  second place from top to variable functionNameFromTestFileInStackTrace
  //  5. In the same window, you will see the line number of the function in the stack trace window.
  //  assign the same to lineNumberFromTestFileInStackTrace
  //  Once you are done with above, just run the corresponding test and
  //  make sure its working as expected. use below command to do the same.
  //  ./gradlew test --tests PortfolioManagerApplicationTest.testDebugValues

  public static List<String> debugOutputs() {

     String valueOfArgument0 = "trades.json";
     String resultOfResolveFilePathArgs0 = "";
     String toStringOfObjectMapper = "";
     String functionNameFromTestFileInStackTrace = "";
     String lineNumberFromTestFileInStackTrace = "";


    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }


  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException, RuntimeException {

    String tradeJsonName = args[0];
    LocalDate endDate = LocalDate.parse(args[1]);
    File tradeJsonPath = resolveFileFromResources(tradeJsonName);

    ObjectMapper objectMapper = getObjectMapper();

    PortfolioTrade[] pfTrades = objectMapper.readValue(tradeJsonPath, PortfolioTrade[].class);

    List<TotalReturnsDto> totalReturnsDtos = new ArrayList<>();
    List<String> finalSortedSymbols = new ArrayList<>();

    for(PortfolioTrade currTrade: pfTrades)
    {
      String url = prepareUrl(currTrade, endDate, "088fb67c203dc236a124f65dfff1e513a6285658");
      RestTemplate restTemplate = new RestTemplate();

      TiingoCandle[] tingoCandles = restTemplate.getForObject(url, TiingoCandle[].class);
      if(tingoCandles != null)
      {
        totalReturnsDtos.add(new TotalReturnsDto(currTrade.getSymbol(), tingoCandles[tingoCandles.length-1].getClose()));
      }
      
    }

    Collections.sort(totalReturnsDtos);

    for(TotalReturnsDto totalReturnsDto: totalReturnsDtos)
    {
      finalSortedSymbols.add(totalReturnsDto.getSymbol());
    }

    return finalSortedSymbols;
  }

  // TODO:
  //  After refactor, make sure that the tests pass by using these two commands
  //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    
    File tradeFromJson = resolveFileFromResources(filename);
    ObjectMapper objMapper = getObjectMapper();

    PortfolioTrade[] pfTrade = objMapper.readValue(tradeFromJson, PortfolioTrade[].class);
    List<PortfolioTrade> tradeList = new ArrayList<>();

    for(PortfolioTrade trade: pfTrade)
    {
      tradeList.add(trade);
    }
    
    // return Collections.emptyList();
    return tradeList;
  }


  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    //  return Collections.emptyList();
    LocalDate startDate = trade.getPurchaseDate();
    if(startDate.isAfter(endDate))
    {
      throw new RuntimeException();
    }
    String url = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate="+ trade.getPurchaseDate().toString() + "&endDate=" + endDate.toString() +"&token=" + token;

    return url;
  }










  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());


    printJsonObject(mainReadQuotes(args));


  }
}

