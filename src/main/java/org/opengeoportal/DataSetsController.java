package org.opengeoportal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Creates a resource controller
 * which handles a GET request
 * for '/datasets' and returns a
 * DataSet resource.
 *
 * @author Joana Simoes
 * @version 1.0
 * @since   2017-01-13
 */
@Controller
@RequestMapping("/datasets")
public class DataSetsController {

    /**
     * Delivers a DataSets instance, or a default string.
     * @param name default return string.
     * @return dataset list.
     */
    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody
    final DataSets getDataSets(@RequestParam(value = "name", required = true,
            defaultValue = "Sorry, no datasets available!") final String name) {
                return new DataSets(name);
    }

}
