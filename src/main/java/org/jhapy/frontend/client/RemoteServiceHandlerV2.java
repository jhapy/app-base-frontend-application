package org.jhapy.frontend.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.jhapy.commons.exception.ErrorConstants;
import org.jhapy.commons.exception.JHapyProblem;
import org.jhapy.commons.security.SecurityUtils;
import org.jhapy.dto.domain.BaseEntity;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.*;
import org.jhapy.dto.utils.AppContextThread;
import org.jhapy.dto.utils.PageDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.zalando.problem.jackson.ProblemModule;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 08/05/2021
 */
public interface RemoteServiceHandlerV2<T extends BaseEntity> {

  @PostMapping(value = "/findAnyMatching")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "findAnyMatchingFallback")
  ServiceResult<PageDTO<T>> findAnyMatching(@RequestBody FindAnyMatchingQuery query);

  default ServiceResult<PageDTO<T>> findAnyMatchingFallback(
      FindAnyMatchingQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("findAnyMatchingFallback"), e, new PageDTO<>());
  }

  @PostMapping(value = "/countAnyMatching")
  @CircuitBreaker(
      name = "defaultServiceCircuitBreaker",
      fallbackMethod = "countAnyMatchingFallback")
  ServiceResult<Long> countAnyMatching(@RequestBody CountAnyMatchingQuery query);

  default ServiceResult<Long> countAnyMatchingFallback(CountAnyMatchingQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("countAnyMatchingFallback"), e, 0L);
  }

  @PostMapping(value = "/getById")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "getByIdFallback")
  ServiceResult<T> getById(@RequestBody GetByIdQuery query);

  default ServiceResult<T> getByIdFallback(GetByIdQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("getByIdFallback"), e, null);
  }

  @PostMapping(value = "/save")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "saveFallback")
  ServiceResult<T> save(@RequestBody SaveQuery<T> query);

  default ServiceResult<T> saveFallback(SaveQuery<T> query, Exception e) {
    return defaultFallback(getLoggerPrefix("saveFallback"), e, null);
  }

  @PostMapping(value = "/saveAll")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "saveAllFallback")
  ServiceResult<List<T>> saveAll(@RequestBody SaveAllQuery<T> query);

  default ServiceResult<List<T>> saveAllFallback(SaveAllQuery<T> query, Exception e) {
    return defaultFallback(getLoggerPrefix("saveAllFallback"), e, null);
  }

  @PostMapping(value = "/getAll")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "getAllFallback")
  ServiceResult<List<T>> getAll(@RequestBody BaseRemoteQuery query);

  default ServiceResult<List<T>> getAllFallback(BaseRemoteQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("getAllFallback"), e, null);
  }

  @PostMapping(value = "/delete")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "deleteFallback")
  ServiceResult<Void> delete(@RequestBody DeleteByIdQuery query);

  default ServiceResult<Void> deleteFallback(DeleteByIdQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("deleteFallback"), e, null);
  }

  default ServiceResult defaultFallback(String loggerPrefix, Exception e, Object defaultResult) {
    error(loggerPrefix, "An error has occurred {0}", e.getLocalizedMessage());
    var objectMapper = new ObjectMapper();
    objectMapper.registerModule(new ProblemModule());
    try {
      if (e instanceof FeignException) {
        if (e instanceof RetryableException) {
          ServiceResult result = new ServiceResult();
          result.setIsSuccess(false);
          result.setMessage("Module not available");
          result.setData(defaultResult);
          return result;
        } else {
          var responseBody =
              new String(
                  ((FeignException) e)
                      .responseBody()
                      .orElseThrow(() -> new Exception("Cannot decode body"))
                      .array());
          JHapyProblem problem = objectMapper.readValue(responseBody, JHapyProblem.class);

          ServiceResult result;
          if (problem.getType().equals(ErrorConstants.SERVICE_EXCEPTION_TYPE)) {
            var serviceName = problem.getServiceName();
            var message = problem.getTitle();
            if (problem.getErrors() != null) {
              message += " : " + String.join(", ", Arrays.asList(problem.getErrors()));
            }
            result = new ServiceResult<>(false, message, defaultResult);
            result.setMessageTitle(serviceName);
          } else {
            result =
                new ServiceResult<>(
                    false,
                    StringUtils.isNotBlank(problem.getDetail())
                        ? problem.getDetail()
                        : problem.getMessage(),
                    defaultResult);
            result.setMessageTitle(
                StringUtils.isNotBlank(problem.getTitle())
                    ? problem.getTitle()
                    : problem.getStatus().getReasonPhrase());
          }

          if (problem.getStacktrace() != null) {
            result.setExceptionString(String.join("\n", Arrays.asList(problem.getStacktrace())));
          }
          return result;
        }
      } else {
        return new ServiceResult<>(false, e.getLocalizedMessage(), defaultResult);
      }
    } catch (Exception exception) {
      error(
          loggerPrefix,
          "Unexpected error {0} while decoding remote exception",
          exception.getLocalizedMessage());
      return new ServiceResult<>(false, e.getLocalizedMessage(), defaultResult);
    }
  }

  default String getLoggerPrefix(final String methodName) {
    String username =
        SecurityUtils.getCurrentUserLogin().orElse(AppContextThread.getCurrentUsername());
    String sessionId =
        AppContextThread.getCurrentSessionId() == null
            ? "local"
            : AppContextThread.getCurrentSessionId();
    ThreadContext.put("jhapy.username", username);
    ThreadContext.put("jhapy.sessionId", sessionId);
    var params = "";
    if (StringUtils.isNotBlank(username)) {
      params += username;
    }

    return String.format("%-30s", methodName + "(" + params + ")") + " :: ";
  }

  default String getLoggerPrefix(final String methodName, Object... params) {
    String username =
        SecurityUtils.getCurrentUserLogin().orElse(AppContextThread.getCurrentUsername());
    String sessionId =
        AppContextThread.getCurrentSessionId() == null
            ? "local"
            : AppContextThread.getCurrentSessionId();
    ThreadContext.put("jhapy.username", username);
    ThreadContext.put("jhapy.sessionId", sessionId);
    var paramsStr = new StringBuilder();
    if (StringUtils.isNotBlank(username)) {
      paramsStr.append(username).append(params.length > 0 ? ", " : "");
    }

    if (params.length > 0) {
      for (Object p : params) {
        if (p == null) {
          paramsStr.append("null").append(", ");
        } else if (p.getClass().isArray()) {
          paramsStr.append(Arrays.asList((Object[]) p)).append(", ");
        } else {
          paramsStr.append(p).append(", ");
        }
      }
      paramsStr = new StringBuilder(paramsStr.substring(0, paramsStr.length() - 2));
    }
    return String.format("%-30s", methodName + "(" + paramsStr + ")") + " :: ";
  }

  default Logger logger() {
    return LogManager.getLogger(getClass());
  }

  default void trace(String prefix, String message, Object... params) {
    logger()
        .trace(() -> MessageFormat.format("{0}{1}", prefix, MessageFormat.format(message, params)));
  }

  default void debug(String prefix, String message, Object... params) {
    logger()
        .debug(() -> MessageFormat.format("{0}{1}", prefix, MessageFormat.format(message, params)));
  }

  default void info(String prefix, String message, Object... params) {
    logger()
        .info(() -> MessageFormat.format("{0}{1}", prefix, MessageFormat.format(message, params)));
  }

  default void warn(String prefix, String message, Object... params) {
    logger()
        .warn(() -> MessageFormat.format("{0}{1}", prefix, MessageFormat.format(message, params)));
  }

  default void warn(String prefix, Throwable exception, String message, Object... params) {
    logger()
        .warn(
            () -> MessageFormat.format("{0}{1}", prefix, MessageFormat.format(message, params)),
            exception);
  }

  default void error(String prefix, String message, Object... params) {
    logger()
        .error(() -> MessageFormat.format("{0}{1}", prefix, MessageFormat.format(message, params)));
  }

  default void error(String prefix, Throwable exception, String message, Object... params) {
    logger()
        .error(
            () -> MessageFormat.format("{0}{1}", prefix, MessageFormat.format(message, params)),
            exception);
  }

  default Logger logger(Class aClass) {
    return LogManager.getLogger(aClass);
  }
}