package tn.esprit.similator.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.similator.entity.Holding;
import tn.esprit.similator.service.IHoldingService;
import tn.esprit.similator.service.PortfolioServImpl;

import java.util.List;

@Tag(name = "Holding class")
@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/holding")
public class HoldingController {


    IHoldingService holdingServ;
    PortfolioServImpl portfolioServ;
    // -------------------CRUD----------------------
    @GetMapping("/Get-all-holdings")
    public List<Holding> getHoldings() {
        List<Holding> listUtsers = holdingServ.retrieveAllHoldings();
        return listUtsers;
    }
    @GetMapping("/Get-holding/{holding-id}")
    public Holding retrieveHolding(@PathVariable("holding-id") Long holdingId) {
        Holding holding = holdingServ.retrieveHolding(holdingId);
        return holding;
    }
    @PostMapping("/Add-Holding")
    public Holding addHolding(@RequestBody Holding u) {
        Holding holding = holdingServ.addHolding(u);
        return holding;
    }
    @PutMapping("/modify-holding")
    public Holding modifyHolding(@RequestBody Holding usr) {
        Holding holding = holdingServ.modifyHolding(usr);
        return holding;
    }
    @DeleteMapping("/remove-holding/{holding-id}")
    public void removeHolding(@PathVariable("holding-id") Long holdingId) {
        holdingServ.removeHolding(holdingId);
    }

    // -------------------OTHER METHOD----------------------

    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<List<Holding>> getHoldingsByPortfolioId(@PathVariable Long portfolioId) {
        List<Holding> holdings = holdingServ.getHoldingsByPortfolioId(portfolioId);
        if (holdings.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(holdings);
    }



    /*
    @GetMapping("/{portfolioId}/value")
    public ResponseEntity<Portfolio> getPortfolioValue(@PathVariable Long portfolioId) throws JSONException {

            // Calculate and fetch the portfolio value using the service
            Portfolio portfolio = portfolioServ.calculatePortfolioValue(portfolioId);
            return ResponseEntity.ok(portfolio); // Return the portfolio with the updated value


    }

     */



}
