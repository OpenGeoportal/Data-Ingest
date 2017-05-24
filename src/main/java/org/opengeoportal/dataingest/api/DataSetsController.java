package org.opengeoportal.dataingest.api;

import it.geosolutions.geoserver.rest.decoder.RESTDataStore;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.opengeoportal.dataingest.api.download.LocalDownloadService;
import org.opengeoportal.dataingest.api.fileCache.LRUFileCache;
import org.opengeoportal.dataingest.api.upload.LocalUploadService;
import org.opengeoportal.dataingest.exception.CacheCapacityException;
import org.opengeoportal.dataingest.exception.FeatureSizeFormatException;
import org.opengeoportal.dataingest.exception.FileNotReadyException;
import org.opengeoportal.dataingest.exception.ForcedSRSFormatException;
import org.opengeoportal.dataingest.exception.GeoServerException;
import org.opengeoportal.dataingest.exception.NoDataFoundOnGeoserverException;
import org.opengeoportal.dataingest.exception.PageFormatException;
import org.opengeoportal.dataingest.exception.PageNotFoundException;
import org.opengeoportal.dataingest.exception.PageSizeFormatException;
import org.opengeoportal.dataingest.exception.RequestNotPresentException;
import org.opengeoportal.dataingest.exception.ShapefilePackageException;
import org.opengeoportal.dataingest.exception.WFSException;
import org.opengeoportal.dataingest.utils.DatasetsPageWrapper;
import org.opengeoportal.dataingest.utils.FileConversionUtils;
import org.opengeoportal.dataingest.utils.FileNameUtils;
import org.opengeoportal.dataingest.utils.GeoServerRESTFacade;
import org.opengeoportal.dataingest.utils.GeoServerUtils;
import org.opengeoportal.dataingest.utils.ResultSortedPaginator;
import org.opengeoportal.dataingest.utils.ShapeFileValidator;
import org.opengeoportal.dataingest.utils.TicketGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Creates a resource controller which handles the various GET, DELETE, POST and
 * PUT request of the REST API.
 *
 * @author Joana Simoes
 * @version 1.0
 * @since 2017-01-13
 */
@Controller
public class DataSetsController {

    /**
     * Stores a hash of the list of layers.
     */
    private final int oldLayerList = -1;
    /**
     * Cache service.
     */
    @Autowired
    private CacheService service;
    /**
     * The GeoServer URL (from the application.properties).
     */
    @Value("${geoserver.url}")
    private String geoserverUrl;

    /**
     * The GeoServer Username (from the application.properties).
     */
    @Value("${geoserver.username}")
    private String geoserverUsername;

    /**
     * The GeoServer Password (from the application.properties).
     */
    @Value("${geoserver.password}")
    private String geoserverPassword;

    /**
     * The page size for dataset results (from the application.properties).
     */
    @Value("${param.dataset.pagesize}")
    private int pageSize;

    /**
     * Disk path of the file cache (where we store the physical files).
     */
    @Value("${cache.path}")
    private String path;
    /**
     * Name of the cache, which is the directory to be created on given path.
     */
    @Value("${cache.name}")
    private String cachename;

    /**
     * localDownloadService.
     */
    @Autowired
    private LocalDownloadService localDownloadService;

    /**
     * localUploadService.
     */
    @Autowired
    private LocalUploadService localUploadService;

    /**
     * A file cache, following the LRU eviction policy.
     */
    @Autowired
    private LRUFileCache fileCache;

    /**
     * Sets the file cache.
     */
    @Autowired
    public void setFileCache() {
        this.localDownloadService.setFileCache(fileCache);
    }

    /**
     * Unpaginated version of the get datasets request.
     *
     * @param request  the request (no arguments)
     * @param response the http response
     * @return the list of datasets as a an angular.js friendly list of key values
     * @throws Exception
     */
    @RequestMapping(value = "/allDatasets", method = RequestMethod.GET)
    @ResponseBody
    public final Map<String, List<Map<String, String>>> getAllDataSets(
        final HttpServletRequest request,
        final HttpServletResponse response) throws Exception {
        List<Map<String, String>> result =  service.getDatasets(geoserverUrl);
        Map<String, List<Map<String, String>>> map = new HashMap<String, List<Map<String, String>>>();
        map.put("data", result);
        return map;
    }

    /**
     * Mockup of the Unpaginated version of the get datasets request.
     * Smaller request, filtered for the 'db' workspace, for test purposes.
     *
     * @param request  the request (no arguments)
     * @param response the http response
     * @return the list of datasets as a an angular.js friendly list of key values
     * @throws Exception
     */
    @RequestMapping(value = "/allDatasetsMockup", method = RequestMethod.GET)
    @ResponseBody
    public final Map<String, List<Map<String, String>>> getAllDataSetsMockup(
        final HttpServletRequest request,
        final HttpServletResponse response) throws Exception {
        List<Map<String, String>> result =  service.getDatasets(geoserverUrl + "topp" + "/");
        Map<String, List<Map<String, String>>> map = new HashMap<String, List<Map<String, String>>>();
        map.put("data", result);
        return map;
    }

    /**
     * Gets the data sets from all workspaces.
     *
     * @param request  the request
     * @param response the response
     * @return the data sets
     * @throws Exception the exception
     */
    @RequestMapping(value = "/datasets", method = RequestMethod.GET)
    @ResponseBody
    public final DatasetsPageWrapper getDataSets(
        final HttpServletRequest request,
        final HttpServletResponse response) throws Exception {

        try {
            return getPaginatedDataSets(request, null);
        } catch (final PageNotFoundException pnfex) {
            printOutputMessage(response, HttpServletResponse.SC_NOT_FOUND,
                "Page " + pnfex.getPageNumber() + " not found");
            return null;
        } catch (final NoDataFoundOnGeoserverException ndfgsex) {
            printOutputMessage(response, HttpServletResponse.SC_NOT_FOUND,
                "No data");
            return null;
        } catch (final PageSizeFormatException psfex) {
            printOutputMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                "Wrong page Size format");
            return null;
        } catch (final PageFormatException psfex) {
            printOutputMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                "Wrong page number format");
            return null;
        }
    }

    /**
     * Gets the data sets for workspace.
     *
     * @param workspace the workspace
     * @param request   the request
     * @param response  the response
     * @return the data sets for workspace
     * @throws Exception the exception
     */
    @RequestMapping(value = "/workspaces/{workspace}/datasets", method = RequestMethod.GET)
    @ResponseBody
    public final DatasetsPageWrapper getDataSetsForWorkspace(
        @PathVariable(value = "workspace") final String workspace,
        final HttpServletRequest request,
        final HttpServletResponse response) throws Exception {

        try {
            return getPaginatedDataSets(request, workspace);
        } catch (final PageNotFoundException pnfex) {
            printOutputMessage(response, HttpServletResponse.SC_NOT_FOUND,
                "Page " + pnfex.getPageNumber() + " not found");
            return null;
        } catch (final NoDataFoundOnGeoserverException ndfgsex) {
            printOutputMessage(response, HttpServletResponse.SC_NOT_FOUND,
                "Workspace " + workspace + " does not exist");
            return null;
        } catch (final PageSizeFormatException psfex) {
            printOutputMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                "Wrong page Size format");
            return null;
        } catch (final PageFormatException psfex) {
            printOutputMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                "Wrong page number format");
            return null;
        }
    }

    /**
     * Gets the paginated data sets.
     *
     * @param request   the request
     * @param workspace the workspace
     * @return the paginated data sets
     * @throws Exception the exception
     */
    private DatasetsPageWrapper getPaginatedDataSets(
        final HttpServletRequest request, String workspace)
        throws Exception {
        try {

            boolean reloadResults = false;
            Integer page = null;

            if (request.getParameter("pageSize") != null) {
                if (request.getParameter("pageSize").isEmpty()) {
                    throw new PageSizeFormatException();
                } else if (!StringUtils
                    .isNumeric(request.getParameter("pageSize"))) {
                    throw new PageSizeFormatException();
                } else {
                    this.pageSize = Integer
                        .parseInt(request.getParameter("pageSize"));
                }
            }

            if (request.getParameter("page") != null) {
                if (request.getParameter("page").isEmpty()) {
                    throw new PageFormatException();
                } else if (!StringUtils
                    .isNumeric(request.getParameter("page"))) {
                    throw new PageFormatException();
                } else {
                    page = Integer.parseInt(request.getParameter("page"));
                }
            }

            if (workspace == null || StringUtils.isEmpty(workspace)) {
                workspace = "*";
            }

            if (page == null) {
                page = 1;
                reloadResults = true;
            }

            if (request.getSession().getAttribute("w_" + workspace) == null) {
                reloadResults = true;
            }

            // pagination
            ResultSortedPaginator paginator = null;

            if (!reloadResults) {
                // manages with HtppSession the navigation between pages
                paginator = (ResultSortedPaginator) request.getSession()
                    .getAttribute("w_" + workspace);
                paginator.setPageSize(this.pageSize);
            } else {
                List<Map<String, String>> resultMap = null;

                if (workspace.equals("*")) {
                    // get data from geoserver
                    resultMap = service.getDatasets(geoserverUrl);
                } else {
                    // get data from geoserver
                    try {
                        resultMap = service
                            .getDatasets(geoserverUrl + workspace + "/");
                    } catch (final Exception e) {
                        throw new NoDataFoundOnGeoserverException();
                    }
                }

                if (resultMap == null || resultMap.size() == 0) {
                    throw new NoDataFoundOnGeoserverException();
                }

                // Setup the paginator
                paginator = new ResultSortedPaginator(resultMap, this.pageSize,
                    true);
            }

            if (page > paginator.getMaxPages()) {
                throw new PageNotFoundException(page);
            }

            paginator.setPage(page);

            return new DatasetsPageWrapper(paginator.getHashMapForPage(),
                paginator.getList().size(), paginator.getPage(),
                paginator.getMaxPages());

        } catch (final Exception ex) {
            throw ex;
        }
    }

    /**
     * Gives detailed information about one given dataset.
     *
     * @param workspace given workspace
     * @param dataset   given dataset
     * @param request   the request, so we can parse any parameters
     * @param response  the response
     * @return String dataset info, as a set of properties.
     * @throws Exception the exception
     */
    @RequestMapping(value = "/workspaces/{workspace}/datasets/{dataset}", method = RequestMethod.GET)
    @ResponseBody
    public final HashMap<String, String> getDataSet(
        @PathVariable(value = "workspace") final String workspace,
        @PathVariable(value = "dataset") final String dataset, final HttpServletRequest request,
        final HttpServletResponse response) throws Exception {

        try {

            boolean bIsCached = fileCache.isCached(GeoServerUtils.getTypeName(workspace, dataset));
            boolean bFeatureSize = false; // By default, we dont return the feature size

            if (request.getParameter("featureSize") != null) {
                if (request.getParameter("featureSize").isEmpty()) {
                    throw new FeatureSizeFormatException();
                } else if (BooleanUtils.toBooleanObject(
                    request.getParameter("featureSize")) == null) {
                    throw new FeatureSizeFormatException();
                } else {
                    bFeatureSize = BooleanUtils.toBooleanObject(
                        request.getParameter("featureSize"));
                }
            }

            final HashMap<String, String> data = service.getInfo(geoserverUrl,
                workspace, dataset, bFeatureSize);

            if (data == null && data.size() == 0) {
                throw new NoDataFoundOnGeoserverException();
            }

            // We don't want to use the cache for this
            data.put("cached", String.valueOf(bIsCached));

            //TODO: ows endpoints

            return data;

        } catch (final WFSException wfse) {
            printOutputMessage(response, HttpServletResponse.SC_NOT_FOUND,
                wfse.getMessage());
            return null;
        } catch (final NoDataFoundOnGeoserverException ndfgsex) {
            printOutputMessage(response, HttpServletResponse.SC_NOT_FOUND,
                "Dataset " + dataset + " does not exist");
            return null;
        } catch (final FeatureSizeFormatException fsfex) {
            printOutputMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                "Wrong feature Size format");
            return null;
        } catch (final Exception ioex) {
            ioex.printStackTrace();
            printOutputMessage(response,
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Internal server error.");
            return null;
        }
    }

    /**
     * Deletes a given dataset. It just unpublishes the layer leaving intact the
     * datastore. The underlying dataset is not purged.
     *
     * @param workspace given workspace
     * @param dataset   given dataset
     * @throws Exception the exception
     */
    @RequestMapping(value = "/workspaces/{workspace}/datasets/{dataset}", method = RequestMethod.DELETE)
    @ResponseBody
    public final void deleteDataSet(
        @PathVariable(value = "workspace") final String workspace,
        @PathVariable(value = "dataset") final String dataset)
        throws Exception {


        GeoServerRESTFacade geoServerFacade = new GeoServerRESTFacade(geoserverUrl, geoserverUsername,
            geoserverPassword);

        RESTDataStore data = geoServerFacade.getDatastore(workspace, dataset);

        // Unpublish feature type
        if (!geoServerFacade.unpublishFeatureType(workspace, data.getName(),
            dataset)) {
            throw new Exception("Could not unpublish featuretype " + dataset
                + " on store " + data.getName());
        }
        geoServerFacade.reload();

        // Clear this file from the caches
        service.clearCache(geoserverUrl, workspace, dataset);
        String typeName = GeoServerUtils.getTypeName(workspace, dataset);
        if (fileCache.isCached(typeName)) {
            fileCache.remove(typeName);
        }

    }

    /**
     * Uploads a given dataset.
     * <p>
     * Test with curl  -v -F file=@/tmp/top_states/topp_antos.zip -X
     * POST http://localhost:8080/workspaces/topp/datasets/antos
     *
     * @param workspace given workspace
     * @param dataset   given dataset
     * @param file      the file
     * @param response  the response
     * @param request   the request
     * @throws Exception the exception
     */
    @RequestMapping(value = "/workspaces/{workspace}/datasets/{dataset}", method = RequestMethod.POST)
    @ResponseBody
    public final void uploadDataSet(
        @PathVariable(value = "workspace") final String workspace,
        @PathVariable(value = "dataset") final String dataset,
        @RequestParam("file") MultipartFile file,
        final HttpServletResponse response,
        final HttpServletRequest request)
        throws Exception {

        // Retrieve store parameter, if it exists
        String store = null;
        if (request.getParameter("store") != null && !request.getParameter("store").isEmpty()) {
            store = request.getParameter("store");
        }

        // If present any check on the SRS of the shape file will be skipped
        String forcedSRS = null;

        try {
            // Forced SRS
            if (request.getParameter("forcedSRS") != null && !request.getParameter("forcedSRS").isEmpty()) {
                if (!StringUtils.isNumeric(request.getParameter("forcedSRS"))) {
                    throw new ForcedSRSFormatException();
                }
                forcedSRS = "EPSG:" + request.getParameter("forcedSRS");
            }
        } catch (ForcedSRSFormatException fex) {
            printOutputMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                "Wrong SRS format: it must be a numerical code");
            return;
        }

        // File Validation
        File zipFile;

        String strEpsg;
        try {
            zipFile = FileConversionUtils.multipartToFile(file);
            if (forcedSRS == null) {
                strEpsg = ShapeFileValidator.isAValidShapeFile(zipFile, true);
            } else {
                strEpsg = forcedSRS;
                ShapeFileValidator.isAValidShapeFile(zipFile, false);
            }
        } catch (IOException ioex) {

            printOutputMessage(response,
                HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                "File not valid");

            return;
        } catch (ShapefilePackageException shpfex) {

            printOutputMessage(response,
                HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                shpfex.getMessage());

            return;
        }

        // GeoserverValidation and send file
        GeoServerRESTFacade geoServerFacade =
            new GeoServerRESTFacade(geoserverUrl, geoserverUsername, geoserverPassword);

        if (geoServerFacade.existsWorkspace(workspace)) {

            try {

                // This layer already exists in this workspace
                if (geoServerFacade.existsLayer(workspace, dataset, true)) {
                    printOutputMessage(response,
                        HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                        "Dataset '" + dataset
                            + "' already exists in workspace " + workspace);
                    throw new Exception();
                }

                // You did not pass a store, but a store exists with this name
                if ((store == null || store.isEmpty()) && geoServerFacade.existsDatastore(workspace, dataset)) {
                    printOutputMessage(response,
                        HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                        "If you want to upload " + dataset + " to an existing store, please "
                            + " pass the 'store' parameter. Otherwise, make sure there is no datastore "
                    + "named '" + dataset + "'.");
                    throw new Exception();
                }
                // You passed a store parameter, but the store does not exist
                if ((store != null && !store.isEmpty()) && !geoServerFacade.existsDatastore(workspace, store)) {
                    printOutputMessage(response,
                        HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                        "There is no store: '" + store + "' on the "
                            + workspace + " workspace");
                    throw new Exception();
                }

                // If we arrived here, then everything is fine. Let's proceed with the upload.
                long ticket = TicketGenerator.openATicket();
                localUploadService.uploadFile(workspace, ((store != null && !store.isEmpty()) ? store : dataset),
                    dataset, zipFile, strEpsg, ticket, false);
                printOutputMessage(response,
                    HttpServletResponse.SC_ACCEPTED,
                    ticket + "* Request for unpload sent. To check status /checkUploadStatus/" + ticket);

                // Uploading data triggers a cache eviction, in order to have the complete dataset list
                service.clearCacheAll();

            } catch (Exception ex) {
                return;
            }

        } else {
            printOutputMessage(response,
                HttpServletResponse.SC_NOT_FOUND,
                "Workspace '" + workspace + "' does not exists.");
            return;
        }
    }

    /**
     * Updates a given dataset.
     * <p>
     * Test with curl  -v -F file=@/tmp/top_states/topp_antos.zip -X
     * PUT http://localhost:8080/workspaces/topp/datasets/antos
     *
     * @param workspace given workspace
     * @param dataset   given dataset
     * @param file      the file
     * @param response  the response
     * @param request   the request
     * @throws Exception the exception
     */
    @RequestMapping(value = "/workspaces/{workspace}/datasets/{dataset}", method = RequestMethod.PUT)
    @ResponseBody
    public final void updateDataSet(
        @PathVariable(value = "workspace") final String workspace,
        @PathVariable(value = "dataset") final String dataset,
        @RequestParam("file") MultipartFile file,
        final HttpServletResponse response,
        final HttpServletRequest request)
        throws Exception {

        // If present any check on the SRS of the shape file will be skipped
        String forcedSRS = null;

        // Forced SRS
        if (request.getParameter("forcedSRS") != null && !request.getParameter("forcedSRS").isEmpty()) {
            forcedSRS = request.getParameter("forcedSRS");
        }

        // File Validation
        File zipFile;
        String strEpsg;

        try {
            zipFile = FileConversionUtils.multipartToFile(file);

            if (forcedSRS == null) {
                strEpsg = ShapeFileValidator.isAValidShapeFile(zipFile, true);
            } else {
                strEpsg = forcedSRS;
                ShapeFileValidator.isAValidShapeFile(zipFile, false);
            }
        } catch (IOException ioex) {
            printOutputMessage(response,
                HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                "File not valid");

            return;
        } catch (ShapefilePackageException shpfex) {

            printOutputMessage(response,
                HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                shpfex.getMessage());

            return;
        }

        // GeoserverValidation and send file
        GeoServerRESTFacade geoServerFacade = new GeoServerRESTFacade(geoserverUrl,
            geoserverUsername, geoserverPassword);

        String store = null;
        if (geoServerFacade.existsWorkspace(workspace)) {
            try {
                if (!geoServerFacade.existsLayer(workspace, dataset, true)) {
                    printOutputMessage(response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Could not find dataset '" + dataset + "' in workspace:"
                            + workspace + ".");
                    throw new Exception();
                }
                RESTDataStore ds = geoServerFacade.getDatastore(workspace, dataset);
                if (ds == null) {
                    printOutputMessage(response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Could not find a valid store for layer '" + dataset + "' in workspace:"
                            + workspace + ".");
                    throw new Exception();
                }
                store = ds.getName();

            } catch (Exception ex) {
                return;
            }

            Assert.isTrue(store != null, "store name must not be null");

            long ticket = TicketGenerator.openATicket();

            localUploadService.uploadFile(workspace, store, dataset, zipFile, strEpsg, ticket, true);
            printOutputMessage(response,
                HttpServletResponse.SC_ACCEPTED,
                ticket + "* Request for update sent. To check status /checkUploadStatus/" + ticket);
        } else {
            printOutputMessage(response,
                HttpServletResponse.SC_NOT_FOUND,
                "Workspace '" + workspace + "' does not exists.");
            return;
        }

        // Clear this file from the caches
        service.clearCache(geoserverUrl, workspace, dataset);
        String typeName = GeoServerUtils.getTypeName(workspace, dataset);
        if (fileCache.isCached(typeName)) {
            fileCache.remove(typeName);
        }
    }

    /**
     * Give the status of UPLOAD/UPDATE request
     *
     * @param ticket   the ticket
     * @param response the response
     * @throws Exception the exception
     */
    @RequestMapping(value = "/checkUploadStatus/{ticket}", method = RequestMethod.GET)
    @ResponseBody
    public final void checkUploadStatus(
        @PathVariable(value = "ticket") final long ticket, HttpServletResponse response)
        throws Exception {

        try {
            if (TicketGenerator.isClosed(ticket)) {
                printOutputMessage(response, HttpServletResponse.SC_OK,
                    "File uploaded.");
                return;
            } else {
                printOutputMessage(response, HttpServletResponse.SC_ACCEPTED,
                    "Upload in progress.");
                return;
            }
        } catch (RequestNotPresentException rnpex) {
            printOutputMessage(response, HttpServletResponse.SC_NOT_FOUND,
                "Request not found.");
            return;
        } catch (GeoServerException gsex) {
            printOutputMessage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "The request ended with an error from GeoServer: " + gsex.getMsg());
            return;
        }

    }

    /**
     * Download a ZIP file with the requested dataset.
     *
     * @param workspace the needed workspace
     * @param dataset   the needed dataset
     * @param response  http response
     * @throws Exception the exception
     */
    @RequestMapping(value = "/workspaces/{workspace}/datasets/{dataset}/download", method = RequestMethod.GET)
    @ResponseBody
    public final void download(
        @PathVariable(value = "workspace") final String workspace,
        @PathVariable(value = "dataset") final String dataset,
        final HttpServletResponse response) throws Exception {
        File file = null;
        try {
            file = localDownloadService.getFile(workspace, dataset);
        } catch (final FileNotFoundException fnfex) {
            printOutputMessage(response, HttpServletResponse.SC_NOT_FOUND,
                "Dataset " + dataset + " does not exist");
            return;
        } catch (final FileNotReadyException fnrex) {
            printOutputMessage(response, HttpServletResponse.SC_ACCEPTED,
                "File not yet ready.");
            return;
        } catch (final CacheCapacityException ccex) {
            printOutputMessage(response, HttpServletResponse.SC_PRECONDITION_FAILED, ccex.getMessage());
            return;
        } catch (final IOException ioex) {
            ioex.printStackTrace();
            printOutputMessage(response,
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Internal server error.");
            return;
        }

        // Setting up the headers:
        // The content type
        response.setContentType("application/force-download");
        // The file size
        response.addHeader("Content-Length", Long.toString(file.length()));
        // binary encoding
        response.setHeader("Content-Transfer-Encoding", "binary");
        // Filename
        response.setHeader("Content-Disposition", "attachment;filename=\""
            + FileNameUtils.getZipFileName(dataset));
        InputStream is = null;

        try {
            // get your file as InputStream
            is = new FileInputStream(file);
            // copy it to response's OutputStream
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        } finally {
            org.apache.commons.io.IOUtils.closeQuietly(is);

            try {
                if (!file.getParent().equals(FileNameUtils.getCachePath(path, cachename))) {
                    File dir = new File(file.getParent());
                    file.delete();
                    dir.delete();
                }
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    /**
     * Prints the output message.
     *
     * @param response the response
     * @param code     the code
     * @param message  the message
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void printOutputMessage(final HttpServletResponse response,
                                    final int code, final String message) throws IOException {
        response.setStatus(code);
        final PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        out.println(message);
        response.flushBuffer();
    }

}
