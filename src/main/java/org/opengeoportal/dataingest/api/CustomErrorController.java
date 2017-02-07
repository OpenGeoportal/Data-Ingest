package org.opengeoportal.dataingest.api;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Created by joana on 23/01/17.
 */

@RestController
@RequestMapping("/error")
public class CustomErrorController
    implements org.springframework.boot.autoconfigure.web.ErrorController {
  /**
   * Attributes of the error.
   */
  private final ErrorAttributes errorAttributes;

  /**
   * Custom controller to handle errors.
   *
   * @param errorAttributes
   *          error attributes.
   */
  @Autowired
  public CustomErrorController(final ErrorAttributes errorAttributes) {
    Assert.notNull(errorAttributes, "ErrorAttributes must not be null");
    this.errorAttributes = errorAttributes;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.springframework.boot.autoconfigure.web.ErrorController#getErrorPath()
   */
  @Override
  public final String getErrorPath() {
    return "/error";
  }

  /**
   * Error message.
   *
   * @param aRequest
   *          http request.
   * @return body with error attributes.
   */
  @RequestMapping
  public final Map<String, Object> error(final HttpServletRequest aRequest) {
    final Map<String, Object> body = getErrorAttributes(aRequest,
        getTraceParameter(aRequest));
    final String trace = (String) body.get("trace");
    if (trace != null) {
      final String[] lines = trace.split("\n\t");
      body.put("trace", lines);
    }
    return body;
  }

  /**
   * Get trace parameter.
   *
   * @param request
   *          http request.
   * @return boolean indicating if it gets the parameter.
   */
  private boolean getTraceParameter(final HttpServletRequest request) {
    final String parameter = request.getParameter("trace");
    if (parameter == null) {
      return false;
    }
    return !"false".equals(parameter.toLowerCase());
  }

  /**
   * Get error attributes.
   *
   * @param aRequest
   *          http request.
   * @param includeStackTrace
   *          boolean to indicate if it should include the stack trace.
   * @return map with error attributes and its values.
   */
  private Map<String, Object> getErrorAttributes(
      final HttpServletRequest aRequest, final boolean includeStackTrace) {
    final RequestAttributes requestAttributes = new ServletRequestAttributes(
        aRequest);
    return errorAttributes.getErrorAttributes(requestAttributes,
        includeStackTrace);
  }
}
