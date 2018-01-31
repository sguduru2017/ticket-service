package com.sguduru.ticketservice;
 



import org.json.JSONObject;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.sguduru.ticketservice.TicketServiceApplication;
import com.sguduru.ticketservice.model.SeatHold;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TicketServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TicketServiceTest {

	@LocalServerPort
	private int port;
	
	@Value("${venue.capacity}")
	private int venueCapacity;
	
	@Value("${venue.expireinterval}")
	private int holdExpireSeconds;
	
	private int holdSeatTest=0;
	private int holdSeats=4;

	public static final Logger logger = LoggerFactory.getLogger(TicketServiceTest.class);

	TestRestTemplate restTemplate = new TestRestTemplate();
	HttpHeaders headers = new HttpHeaders();

	@Test
	public void getSeatsAvailable() {

		logger.info("#### Get Available Seats Test-1 #####");
		String expected = Integer.toString(venueCapacity);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		ResponseEntity<Integer> response = restTemplate.exchange(
				createURLWithPort("/ticketservice/status"),
				HttpMethod.GET, entity, Integer.class);

		JSONAssert.assertEquals(expected, response.getBody().toString(), false);
	}
	
	@Test
	public void holdSeats() {

		logger.info("##### Hold Seats [Valid] Test-1 #####");
		holdSeatTest=1;
		int remainingSeating = venueCapacity-holdSeats;
		
		String venueSeating = Integer.toString(venueCapacity);
		String expectedSeating = Integer.toString(remainingSeating);
		
		SeatHold seatHold = new SeatHold(holdSeats,"noname@gmail.com");
		seatHold.setSeatHoldId(holdSeatTest); //expected value of seat hold id
		logger.info("##### TicketServiceTest::holdSeats - SeatHold Request ##### : " + seatHold);

		JSONObject expected = createJSONObject(seatHold);

		HttpEntity<SeatHold> entity = new HttpEntity<SeatHold>(seatHold, headers);
		ResponseEntity<SeatHold> holdSeatResponse = restTemplate.exchange(
				createURLWithPort("/ticketservice/holdseats"),
				HttpMethod.POST, entity, SeatHold.class);
		
		logger.info("##### TicketServiceTest::holdSeats - SeatHold Response ##### : " + seatHold);

		JSONObject actual = createJSONObject(holdSeatResponse.getBody());
		JSONAssert.assertEquals(expected, actual, true);
		
		//Verify the remaining seats
		logger.info("##### Get Available Seats Test-2 #####");
		ResponseEntity<Integer> seatingResponse = restTemplate.exchange(
				createURLWithPort("/ticketservice/status"),
				HttpMethod.GET, entity, Integer.class);

		JSONAssert.assertEquals(expectedSeating, seatingResponse.getBody().toString(), false);
		
		logger.info("##### Get Available Seats Test-3 ##### (wait until hold request expires [" + holdExpireSeconds + "] seconds");
		//Wait for the expired interval and check for the hold size
		try {
			Thread.sleep(holdExpireSeconds*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		seatingResponse = restTemplate.exchange(
				createURLWithPort("/ticketservice/status"),
				HttpMethod.GET, entity, Integer.class);

		JSONAssert.assertEquals(venueSeating, seatingResponse.getBody().toString(), false);

	}
	

	@Test
	public void holdSeatsMoreThanAvailable() {

		logger.info("##### Hold Seats [Invalid] Test-2 #####");
		int holdSeats=venueCapacity+1;
		String message =  "Seats requested exceed the capacity of remaining seats";
		SeatHold seatHold = new SeatHold(holdSeats,"noname@gmail.com");
		
		HttpEntity<SeatHold> entity = new HttpEntity<SeatHold>(seatHold, headers);
		ResponseEntity<?> holdSeatResponse = restTemplate.exchange(
				createURLWithPort("/ticketservice/holdseats"),
				HttpMethod.POST, entity, String.class);
		
		Assert.assertTrue(holdSeatResponse.getBody().toString().contains(message));
	}

	@Test
	public void reserveSeats() {

		logger.info("##### Reserve Seats [Valid] Test-1 #####");
		holdSeatTest=2;
		SeatHold seatHold = new SeatHold(holdSeats,"noname@gmail.com");
		seatHold.setSeatHoldId(holdSeatTest); //expected value of seat hold id

		JSONObject expected = createJSONObject(seatHold);

		HttpEntity<SeatHold> entity = new HttpEntity<SeatHold>(seatHold, headers);
		
		ResponseEntity<SeatHold> holdSeatResponse = restTemplate.exchange(
				createURLWithPort("/ticketservice/holdseats"),
				HttpMethod.POST, entity, SeatHold.class);
		
		JSONObject actual = createJSONObject(holdSeatResponse.getBody());
		JSONAssert.assertEquals(expected, actual, true);
		
		entity = new HttpEntity<SeatHold>(holdSeatResponse.getBody(), headers);
		ResponseEntity<String> reserveSeatResponse = restTemplate.exchange(
				createURLWithPort("/ticketservice/reserveseats"),
				HttpMethod.POST, entity, String.class);

		String correlationId = reserveSeatResponse.getBody().toString();
		logger.info("Seat Reserve CorrelationId: " + correlationId);
		Assert.assertFalse(null == correlationId);
	}
	
	@Test
	public void reserveSeatWithInvalidHoldId()
	{
		logger.info("##### Reserve Seats [Invalid] Test-2 #####");

		//Reserve with invalid HoldSeatId
		holdSeatTest=3;
		SeatHold seatHold = new SeatHold(holdSeatTest,"noname@gmail.com");
		seatHold.setSeatHoldId(holdSeatTest); //expected value of seat hold id

		String message =  "The holdId is either invalid or expired";

		seatHold.setSeatHoldId(holdSeatTest); //expected value of seat hold id
		HttpEntity<SeatHold>  entity = new HttpEntity<SeatHold>(seatHold, headers);

		ResponseEntity<?> errorResponse = restTemplate.exchange(
				createURLWithPort("/ticketservice/reserveseats"),
				HttpMethod.POST, entity, String.class);
		
		Assert.assertTrue(errorResponse.getBody().toString().contains(message));
	}

	
	private JSONObject createJSONObject(SeatHold seatHold) {
		
		JSONObject _object = new JSONObject();
		_object.put("numSeats", seatHold.getNumSeats());
		_object.put("customerEmail", seatHold.getCustomerEmail());
		_object.put("seatHoldId", seatHold.getSeatHoldId());
		
		return _object;
		
	}
	private String createURLWithPort(String uri) {
		return "http://localhost:" + port + uri;
	}

}
