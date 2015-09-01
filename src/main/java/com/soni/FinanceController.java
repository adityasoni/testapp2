package com.soni;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.annotation.PostConstruct;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Created by asoni1 on 8/23/15.
 */
@Controller
@RequestMapping("/finance")
public class FinanceController {

    private AmazonS3 s3Client;
    private String BUCKET_NAME = "bt-finance-bu";
    private String BUCKET_KEY = "btdetailArray.txt";
    private String PASSPHRASE_BUCKET_KEY = "passphrase.txt";
    private String DATE_FORMAT = "MM-dd-yyyy";
    private String DATE_FORMAT_MON = "dd-MMMMM-yyyy";

    @PostConstruct
    private void initS3Client() {
        BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(
                "A********",
                "P************");

        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setProtocol(Protocol.HTTP);
        s3Client = new AmazonS3Client(basicAWSCredentials, clientConfiguration);

    }

    @RequestMapping(value = "/balances", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(value = HttpStatus.CREATED)
    public
    @ResponseBody
    BalanceTransferDomain postBalance(@RequestBody BalanceTransferDomain balanceTransferDomain,
                                      @RequestParam(value = "email", required = true) final String email,
                                      @RequestParam(value = "password", required = true) final String password) throws IOException {


        authenticate(email+":"+password);

        validateObject(balanceTransferDomain);

        List<BalanceTransferDomain> existingBalanceTransferList = readFileFromS3();

        validateBalanceTransfer(balanceTransferDomain, existingBalanceTransferList);

        balanceTransferDomain.setId(UUID.randomUUID().toString());

        existingBalanceTransferList.add(balanceTransferDomain);

        putObjectOnS3(existingBalanceTransferList);

        return balanceTransferDomain;

    }



    @RequestMapping(value = "/balances", method = RequestMethod.PUT, produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(value = HttpStatus.OK)
    public
    @ResponseBody
    BalanceTransferDomain updateBalanceTransferByName(@RequestBody BalanceTransferDomain balanceTransferDomainToUpdate,
                                                      @RequestParam(value = "email", required = true) final String email,
                                                      @RequestParam(value = "password", required = true) final String password) throws IOException {

        authenticate(email+":"+password);
        boolean doesBalanceTransferDomainExist = false;
        validateObject(balanceTransferDomainToUpdate);
        Collection<BalanceTransferDomain> balanceTransferDomainList = readFileFromS3();

        Iterator<BalanceTransferDomain> balanceTransferDomainIterator = balanceTransferDomainList.iterator();

        while (balanceTransferDomainIterator.hasNext()) {
            BalanceTransferDomain balanceTransferDomain = balanceTransferDomainIterator.next();
            if (balanceTransferDomain.getBankName().equalsIgnoreCase(balanceTransferDomainToUpdate.getBankName())) {
                balanceTransferDomainToUpdate.setId(balanceTransferDomain.getId());
                balanceTransferDomainIterator.remove();
                doesBalanceTransferDomainExist = true;
                break;
            }
        }

        if (doesBalanceTransferDomainExist) {
            List<BalanceTransferDomain> balanceTransferDomainListToUpdate = Lists.newArrayList(balanceTransferDomainList);
            balanceTransferDomainListToUpdate.add(balanceTransferDomainToUpdate);
            putObjectOnS3(balanceTransferDomainListToUpdate);
        } else {
            throw new NoDataFoundException("No Data found", "No Data found");
        }

        return balanceTransferDomainToUpdate;
    }

    @RequestMapping(value = "/balances", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(value = HttpStatus.OK)
    public
    @ResponseBody
    List<BalanceTransferDomain> getBalanceTransferList(@RequestParam(value = "prettyFormat", required = false) final String prettyFormat) throws IOException, ParseException {
        boolean doFormat = true;

        if (StringUtils.isNotBlank(prettyFormat) && prettyFormat.equalsIgnoreCase("no")) {
            doFormat = false;
        }

        List<BalanceTransferDomain> balanceTransferDomainList = readFileFromS3();

        NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
        final SimpleDateFormat simpleDateFormatMON = new SimpleDateFormat(DATE_FORMAT_MON);

        for (BalanceTransferDomain balanceTransferDomain : balanceTransferDomainList) {
            Date dueDate = simpleDateFormat.parse(balanceTransferDomain.getDueDate());

            if (doFormat) {
                balanceTransferDomain.setInitialBalance(numberFormat.format(Integer.valueOf(balanceTransferDomain.getInitialBalance())));
                balanceTransferDomain.setBalanceLeft(numberFormat.format(Integer.valueOf(balanceTransferDomain.getBalanceLeft())));
                balanceTransferDomain.setDueDate(simpleDateFormatMON.format(dueDate));
            }

            balanceTransferDomain.setTimeLeft(updateTimeLeft(dueDate));
        }

        Collections.sort(balanceTransferDomainList, new Comparator<BalanceTransferDomain>() {
            public int compare(BalanceTransferDomain m1, BalanceTransferDomain m2) {
                Date dt1 = new Date();
                Date dt2 = new Date();

                try {
                    if (StringUtils.isNotBlank(prettyFormat) && prettyFormat.equalsIgnoreCase("no")) {
                        dt1 = simpleDateFormat.parse(m1.getDueDate());
                        dt2 = simpleDateFormat.parse(m2.getDueDate());
                    } else {
                        dt1 = simpleDateFormatMON.parse(m1.getDueDate());
                        dt2 = simpleDateFormatMON.parse(m2.getDueDate());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return dt1.compareTo(dt2);
            }
        });

        return balanceTransferDomainList;
    }

    @RequestMapping(value = "/balances/bankname/{bank_name}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(value = HttpStatus.OK)
    public
    @ResponseBody
    BalanceTransferDomain getBalanceTransferByName(@PathVariable("bank_name") final String bankName, @RequestParam(value = "prettyFormat", required = false) String prettyFormat) throws IOException, ParseException {

        boolean doFormat = true;

        if (StringUtils.isNotBlank(prettyFormat) && prettyFormat.equalsIgnoreCase("no")) {
            doFormat = false;
        }

        if (StringUtils.isBlank(bankName)) {
            throw new GenericException("Bank Name can not be null", "Bank Name can not be null");
        }

        List<BalanceTransferDomain> balanceTransferDomainList = readFileFromS3();

        NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
        SimpleDateFormat simpleDateFormatMON = new SimpleDateFormat(DATE_FORMAT_MON);

        for (BalanceTransferDomain balanceTransferDomain : balanceTransferDomainList) {
            if (balanceTransferDomain.getBankName().equalsIgnoreCase(bankName)) {

                Date dueDate = simpleDateFormat.parse(balanceTransferDomain.getDueDate());

                if (doFormat) {
                    balanceTransferDomain.setInitialBalance(numberFormat.format(Integer.valueOf(balanceTransferDomain.getInitialBalance())));
                    balanceTransferDomain.setBalanceLeft(numberFormat.format(Integer.valueOf(balanceTransferDomain.getBalanceLeft())));
                    balanceTransferDomain.setDueDate(simpleDateFormatMON.format(dueDate));
                }

                balanceTransferDomain.setTimeLeft(updateTimeLeft(dueDate));

                return balanceTransferDomain;
            }
        }

        throw new NoDataFoundException("No Data found for Bank Name: " + bankName, "No Data found for Bank Name: " + bankName);

    }

    @RequestMapping(value = "/balances/id/{id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(value = HttpStatus.OK)
    public
    @ResponseBody
    BalanceTransferDomain getBalanceTransferById(@PathVariable("id") final String id, @RequestParam(value = "prettyFormat", required = false) String prettyFormat) throws IOException, ParseException {

        boolean doFormat = true;

        if (StringUtils.isNotBlank(prettyFormat) && prettyFormat.equalsIgnoreCase("no")) {
            doFormat = false;
        }

        if (StringUtils.isBlank(id)) {
            throw new GenericException("Bank Name can not be null");
        }

        List<BalanceTransferDomain> balanceTransferDomainList = readFileFromS3();

        NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
        SimpleDateFormat simpleDateFormatMON = new SimpleDateFormat(DATE_FORMAT_MON);

        for (BalanceTransferDomain balanceTransferDomain : balanceTransferDomainList) {
            if (balanceTransferDomain.getId().equalsIgnoreCase(id)) {
                Date dueDate = simpleDateFormat.parse(balanceTransferDomain.getDueDate());

                if (doFormat) {
                    balanceTransferDomain.setInitialBalance(numberFormat.format(Integer.valueOf(balanceTransferDomain.getInitialBalance())));
                    balanceTransferDomain.setBalanceLeft(numberFormat.format(Integer.valueOf(balanceTransferDomain.getBalanceLeft())));
                    balanceTransferDomain.setDueDate(simpleDateFormatMON.format(dueDate));
                }

                balanceTransferDomain.setTimeLeft(updateTimeLeft(dueDate));

                return balanceTransferDomain;
            }
        }

        throw new NoDataFoundException("No Data found for Bank Id: " + id, "No Data found for Bank Id: " + id);

    }

    @RequestMapping(value = "/balances", method = RequestMethod.DELETE, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(value = HttpStatus.OK)
    public
    @ResponseBody
    List<BalanceTransferDomain> deleteAllBalanceTransferList(
            @RequestParam(value = "email", required = true) final String email,
            @RequestParam(value = "password", required = true) final String password) throws IOException {
        authenticate(email+":"+password);
        List<BalanceTransferDomain> balanceTransferDomainList = Lists.newArrayList();
        putObjectOnS3(balanceTransferDomainList);
        return balanceTransferDomainList;
    }

    @RequestMapping(value = "/balances/bankname/{bank_name}", method = RequestMethod.DELETE, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(value = HttpStatus.OK)
    public
    @ResponseBody
    Void deleteBalanceTransferByName(@PathVariable("bank_name") final String bankName,
                                     @RequestParam(value = "email", required = true) final String email,
                                     @RequestParam(value = "password", required = true) final String password) throws IOException {
        authenticate(email+":"+password);
        if (StringUtils.isBlank(bankName)) {
            throw new GenericException("Bank Name can not be null", "Bank Name can not be null");
        }

        Collection<BalanceTransferDomain> balanceTransferDomainList = readFileFromS3();

        Iterator<BalanceTransferDomain> balanceTransferDomainIterator = balanceTransferDomainList.iterator();

        while (balanceTransferDomainIterator.hasNext()) {
            BalanceTransferDomain balanceTransferDomain = balanceTransferDomainIterator.next();
            if (balanceTransferDomain.getBankName().equalsIgnoreCase(bankName)) {
                balanceTransferDomainIterator.remove();
                break;
            }
        }

        putObjectOnS3(Lists.newArrayList(balanceTransferDomainList));

        return null;
    }

    @RequestMapping(value = "/balances/id/{id}", method = RequestMethod.DELETE, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(value = HttpStatus.OK)
    public
    @ResponseBody
    Void deleteBalanceTransferById(@PathVariable("id") final String id,
                                   @RequestParam(value = "email", required = true) final String email,
                                   @RequestParam(value = "password", required = true) final String password) throws IOException {
        authenticate(email+":"+password);
        if (StringUtils.isBlank(id)) {
            throw new GenericException("Bank Id can not be null", "Bank Id can not be null");
        }

        Collection<BalanceTransferDomain> balanceTransferDomainList = readFileFromS3();

        Iterator<BalanceTransferDomain> balanceTransferDomainIterator = balanceTransferDomainList.iterator();

        while (balanceTransferDomainIterator.hasNext()) {
            BalanceTransferDomain balanceTransferDomain = balanceTransferDomainIterator.next();
            if (balanceTransferDomain.getId().equalsIgnoreCase(id)) {
                balanceTransferDomainIterator.remove();
                break;
            }
        }

        putObjectOnS3(Lists.newArrayList(balanceTransferDomainList));

        return null;
    }

    @RequestMapping(value = "/resource", method = RequestMethod.GET)
    @ResponseBody
    public void getResource(
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String url = "/index.jsp";
        RequestDispatcher rd = request.getRequestDispatcher(url);
        rd.forward(request, response);
    }


    private String updateTimeLeft(Date dueDate) throws ParseException {
        Date today = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy");
        Date today1 = simpleDateFormat.parse(simpleDateFormat.format(today));

        long timeDifferenceMilliseconds = dueDate.getTime() - today1.getTime();

        if (timeDifferenceMilliseconds < 0) {
            return "Overdue";
        }

        long diffDays = timeDifferenceMilliseconds / (60 * 60 * 1000 * 24);

        if (diffDays < 1) {
            return "Overdue";
        }

        long diffWeeks = diffDays / 7;
        long diffMonths = (long) (diffDays / 30.41666666);
        long diffYears = (long) (diffDays / 365.25);


        if (diffWeeks < 1) {
            return diffDays + " days";
        } else if (diffMonths < 1) {

            long daysLeft = diffDays % 7;

            return diffWeeks + " weeks, " + daysLeft + " days";

        } else if (diffYears < 1) {

            long daysLeft = diffDays - (long) (diffMonths * 30.41666666);

            return diffMonths + " months, " + daysLeft + " days";
        } else {
            long monthsLeft = diffMonths - (diffYears * 12);
            return diffYears + " years, " + monthsLeft + " months";
        }
    }

    private void validateBalanceTransfer(BalanceTransferDomain balanceTransferDomainToPut, List<BalanceTransferDomain> existingBalanceTransferList) {
        for (BalanceTransferDomain balanceTransferDomain : existingBalanceTransferList) {
            if (balanceTransferDomain.getBankName().equalsIgnoreCase(balanceTransferDomainToPut.getBankName())) {
                throw new GenericException(
                        String.format("Bank Name <%s> already exists. Please use PUT to update", balanceTransferDomainToPut.getBankName()),
                        String.format("Bank Name <%s> already exists. Please use PUT to update", balanceTransferDomainToPut.getBankName())
                );
            }
        }
    }

    private void validateObject(BalanceTransferDomain balanceTransferDomain) {
        if (null == balanceTransferDomain) {
            throw new GenericException(
                    "balanceTransferDomain can not be null",
                    "balanceTransferDomain can not be null"
            );

        } else if (StringUtils.isBlank(balanceTransferDomain.getBankName())) {
            throw new GenericException("Bank Name can not be null", "Bank Name can not be null");
        } else if (StringUtils.isBlank(balanceTransferDomain.getInitialBalance())) {
            throw new GenericException("Initial Balance can not be null", "Initial Balance can not be null");
        } else if (StringUtils.isBlank(balanceTransferDomain.getBalanceLeft())) {
            throw new GenericException("Balance Left can not be null", "Balance Left can not be null");
        } else if (StringUtils.isBlank(balanceTransferDomain.getDueDate())) {
            throw new GenericException("Due Date can not be null", "Due Date can not be null");
        } else if (!isValidDate(balanceTransferDomain.getDueDate())) {
            throw new GenericException(
                    String.format("Due Date <%s> is not valid", balanceTransferDomain.getDueDate()),
                    String.format("Due Date <%s> is not valid", balanceTransferDomain.getDueDate())
            );
        } else if (!StringUtils.isNumeric(balanceTransferDomain.getInitialBalance())) {
            throw new GenericException(
                    String.format("Initial Balance <%s> is not valid", balanceTransferDomain.getInitialBalance()),
                    String.format("Initial Balance <%s> is not valid", balanceTransferDomain.getInitialBalance())
            );
        } else if (!StringUtils.isNumeric(balanceTransferDomain.getBalanceLeft())) {
            throw new GenericException(
                    String.format("Initial Balance <%s> is not valid", balanceTransferDomain.getBalanceLeft()),
                    String.format("Initial Balance <%s> is not valid", balanceTransferDomain.getBalanceLeft())
            );
        }
    }

    private boolean isValidDate(String dateToValidate) {
        if (StringUtils.isBlank(dateToValidate)) {
            return false;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);

        try {
            simpleDateFormat.parse(dateToValidate);
        } catch (ParseException ex) {
            return false;
        }
        return true;
    }

    private void putObjectOnS3(List<BalanceTransferDomain> balanceTransferDomainList) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        String data = objectMapper.writeValueAsString(balanceTransferDomainList);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data.getBytes());

        s3Client.putObject(BUCKET_NAME, BUCKET_KEY, byteArrayInputStream, new ObjectMetadata());
    }

    private List<BalanceTransferDomain> readFileFromS3() throws IOException {

        S3Object object = s3Client.getObject(
                new GetObjectRequest(BUCKET_NAME, BUCKET_KEY)
        );
        InputStream objectData = object.getObjectContent();
        String stringData = IOUtils.toString(objectData);

        List<BalanceTransferDomain> balanceTransferDomainList = new ObjectMapper().readValue(stringData, new TypeReference<List<BalanceTransferDomain>>() {
        });

        if (balanceTransferDomainList == null) {
            balanceTransferDomainList = Lists.newArrayList();
        }

        return balanceTransferDomainList;
    }

    private void authenticate(String passphrase) throws IOException {
        BASE64Encoder base64Encoder = new BASE64Encoder();
        String encodedPassphrase = base64Encoder.encode(passphrase.getBytes());

        S3Object object = s3Client.getObject(
                new GetObjectRequest(BUCKET_NAME, PASSPHRASE_BUCKET_KEY)
        );
        InputStream objectData = object.getObjectContent();
        String passphraseDataFromS3 = IOUtils.toString(objectData);
        String[] passphraseArrayFromS3 = passphraseDataFromS3.split(",");
        for(String passphraseFromS3 : passphraseArrayFromS3) {
            if(passphraseFromS3.equals(encodedPassphrase)){
                return;
            }
        }

        throw new AuthException("Not Authorized", "Not Authorized");

    }

    @ExceptionHandler({GenericException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ExceptionWrapper handleGenericException(final GenericException ex) {
        return new ExceptionWrapper("400-001", ex.getErrorMessage(), UUID.randomUUID().toString());
    }

    @ExceptionHandler({NoDataFoundException.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    public ExceptionWrapper handleGenericException(final NoDataFoundException ex) {
        return new ExceptionWrapper("404-001", ex.getErrorMessage(), UUID.randomUUID().toString());
    }

    @ExceptionHandler({AuthException.class})
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ExceptionWrapper handleAuthException(final AuthException ex) {
        return new ExceptionWrapper("401-001", ex.getErrorMessage(), UUID.randomUUID().toString());
    }

    @ExceptionHandler({MissingServletRequestParameterException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ExceptionWrapper handleMissingServletRequestParameterException(final Exception ex) {
        return new ExceptionWrapper("400-002", ex.getMessage(), UUID.randomUUID().toString());
    }
}
