package tn.esprit.similator.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.similator.entity.PlacingOrder;
import tn.esprit.similator.entity.Status;
import tn.esprit.similator.service.IPlacingOrderService;

import java.util.List;

@Tag(name = "PlacingOrder class")
@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/placingOrder")
@Slf4j
public class PlaceOrderController {

    IPlacingOrderService placingOrderServ;
    
    @GetMapping("/Get-all-placingOrders")
    public List<PlacingOrder> getPlacingOrders() {
        List<PlacingOrder> listUtsers = placingOrderServ.retrieveAllPlacingOrders();
        return listUtsers;
    }
    
    @GetMapping("/Get-placingOrder/{placingOrder-id}")
    public PlacingOrder retrievePlacingOrder(@PathVariable("placingOrder-id") Long placingOrderId) {
        PlacingOrder placingOrder = placingOrderServ.retrievePlacingOrder(placingOrderId);
        return placingOrder;
    }

    @PostMapping("/{portfolioId}")
    public ResponseEntity<Object> placeOrder(@PathVariable Long portfolioId,
                                             @RequestBody PlacingOrder placingOrder) {
        try {
            PlacingOrder result = placingOrderServ.addPlacingOrderBasedOnMarketStatus(portfolioId, placingOrder);
            return new ResponseEntity<>(result, HttpStatus.CREATED); // Return 201 Created
        } catch (RuntimeException e) {
            log.error("Error placing order: " + e.getMessage(), e);

            // Check if the error message contains information about rate limit
            if (e.getMessage().contains("API rate limit reached")) {
                // Construct the rate limit message
                String rateLimitMessage = "Thank you for using Alpha Vantage! Our standard API rate limit is 25 requests per day. " +
                        "Please subscribe to any of the premium plans at https://www.alphavantage.co/premium/ to instantly remove all daily rate limits.";

                // Return the same message with a 429 status code
                return new ResponseEntity<>(rateLimitMessage, HttpStatus.TOO_MANY_REQUESTS); // Return 429 with message
            }

            // For other runtime exceptions, return 400 Bad Request
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // Return 400 Bad Request
        }
    }


    @GetMapping("/Get-placingOrdersByPortfolio/{portfolioId}")
    public List<PlacingOrder> getPlacingOrdersByPortfolio(@PathVariable Long portfolioId) {
        return placingOrderServ.getOrdersByPortfolioId(portfolioId);
    }
    @PutMapping("/modify-placingOrder")
    public PlacingOrder modifyPlacingOrder(@RequestBody PlacingOrder usr) {
        PlacingOrder placingOrder = placingOrderServ.modifyPlacingOrder(usr);
        return placingOrder;
    }

    @DeleteMapping("/remove-placingOrder/{placingOrder-id}")
    public void removePlacingOrder(@PathVariable("placingOrder-id") Long placingOrderId) {
        placingOrderServ.removePlacingOrder(placingOrderId);
    }

    @PutMapping("/change-status/{orderId}")
    public ResponseEntity<PlacingOrder> changeStatus(@PathVariable Long orderId,
                                                    @RequestParam Status newStatus) {
        try {
            PlacingOrder updatedOrder = placingOrderServ.changeStatus(orderId, newStatus);
            return ResponseEntity.ok(updatedOrder);

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);  // Return 400 if status change is invalid
        }
    }



}



