package com.ezylink.vertx;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(VertXServer.class);
    private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    void handleRequest(final Router router) {
        router.route().handler(BodyHandler.create());

        router.post("/ezylinkapi").handler(this::generateSubscrip);
    }

    private void generateSubscrip(final RoutingContext routingContext) {
        JsonObject input = new JsonObject(routingContext.getBodyAsString());
        JsonObject output;

        if (noMandatoryEmpty(input)) {

            if (!"DAILY".equals(input.getString("subscriptionType")) || !"WEEKLY".equals(input.getString("subscriptionType")) || !"MONTHLY".equals(input.getString("subscriptionType"))) {

                try {
                    Date startDate = DATE_FORMAT.parse(input.getString("startDate"));
                    Date endDate = DATE_FORMAT.parse(input.getString("endDate"));

                    if (endDate.after(startDate)) {
                        Calendar start = Calendar.getInstance();
                        start.setTime(startDate);
                        Calendar end = Calendar.getInstance();
                        end.setTime(endDate);
                        int duration = end.get(Calendar.DAY_OF_YEAR) - start.get(Calendar.DAY_OF_YEAR) ;

                        if (duration < 90) {
                            output = new JsonObject();
                            output.put("amount", input.getDouble("amount"));
                            output.put("subscriptionType", input.getString("subscriptionType"));
                            output.put("invoiceDateList", generateInvoiceDate(input.getString("subscriptionType"), input.getString("dayOfSubscription"), startDate, endDate));
                            routingContext.response().setStatusCode(200).end(output.toString());
                        } else {
                            returnInvalidResponse("Subscription duration is more than 3 motnhs requirement, unable to process.", routingContext);
                        }

                    } else {
                        returnInvalidResponse("Start date is later than end date!", routingContext);
                    }


                } catch (ParseException e) {
                    returnInvalidResponse("An error occur during process subscription, please contact support.", routingContext);
                }

            } else {
                returnInvalidResponse("Invalid subsciption type.", routingContext);
            }

        } else {
            returnInvalidResponse("Contain mandatory field is empty.", routingContext);
        }

    }

    public boolean noMandatoryEmpty(JsonObject input) {
        final boolean isValid = input.getDouble("amount") != null && !input.getString("subscriptionType").isEmpty() && !input.getString("startDate").isEmpty() && !input.getString("endDate").isEmpty();

        return isValid;
    }

    private void returnInvalidResponse(final String message, final RoutingContext routingContext) {
        routingContext.response().setStatusCode(500).end(message);
    }

    private List<String> generateInvoiceDate(String subscriptionType, String subsciptionDay, Date startDate, Date endDate) {
        List<String> result = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int day = 0;
        int dayOfMonth = 0;
        int subDay;

        while(startDate.before(endDate)) {
            calendar.setTime(startDate);
            if ("DAILY".equals(subscriptionType)) {
                result.add(DATE_FORMAT.format(startDate));
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                startDate = calendar.getTime();
            } else if ("WEEKLY".equals(subscriptionType)) {

                if ("SUNDAY".equals(subsciptionDay)) {
                    day = 1;
                } else if ("MONDAY".equals(subsciptionDay)) {
                    day = 2;
                } else if ("TUESDAY".equals(subsciptionDay)) {
                    day = 3;
                } else if ("WEDNESDAY".equals(subsciptionDay)) {
                    day = 4;
                } else if ("THURSDAY".equals(subsciptionDay)) {
                    day = 5;
                } else if ("FRIDAY".equals(subsciptionDay)) {
                    day = 6;
                } else if ("SATURDAY".equals(subsciptionDay)) {
                    day = 7;
                }

                if (day != calendar.get(Calendar.DAY_OF_WEEK)) {
                    if (day > calendar.get(Calendar.DAY_OF_WEEK)) {
                        calendar.add(Calendar.DAY_OF_YEAR, day - calendar.get(Calendar.DAY_OF_WEEK));
                    } else {
                        calendar.add(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_WEEK) - day);
                    }
                }

                startDate = calendar.getTime();
                result.add(DATE_FORMAT.format(startDate));
                calendar.add(Calendar.DAY_OF_YEAR, 7);
                startDate = calendar.getTime();
            } else if ("MONTHLY".equals(subscriptionType)) {

                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                subDay = Integer.valueOf(subsciptionDay);

                if (subDay > dayOfMonth) {
                    calendar.add(Calendar.DAY_OF_MONTH, subDay - dayOfMonth);
                } else if (dayOfMonth > subDay) {
                    calendar.add(Calendar.MONTH, 1);
                    calendar.set(Calendar.DAY_OF_MONTH, subDay);
                }

                startDate = calendar.getTime();
                result.add(DATE_FORMAT.format(startDate));
                calendar.add(Calendar.MONTH, 1);
                startDate = calendar.getTime();

            }

        }

        return result;
    }

}
