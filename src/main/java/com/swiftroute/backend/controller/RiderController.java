package com.swiftroute.backend.controller;
import java.util.List;
import com.swiftroute.backend.model.Rider;
import com.swiftroute.backend.service.RiderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController 
@RequestMapping("/api/riders")
public class RiderController {
    private final RiderService riderService;

    public RiderController(RiderService riderService){
        this.riderService = riderService;
    }
    @GetMapping
public List<Rider> getAllRiders() {
    return riderService.getAllRiders();
}

@PostMapping
public Rider createRider(@RequestBody Rider rider) {
    return riderService.saveRider(rider);
}
@GetMapping("/{id}")
public ResponseEntity<Rider> getRiderById(@PathVariable Long id) {
    return riderService.getRiderById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
}
@PutMapping("/{id}")
public ResponseEntity<Rider> updateRider(
        @PathVariable Long id,
        @RequestBody Rider updatedRider) {

    return riderService.updateRider(id, updatedRider)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
}
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteRider(@PathVariable Long id) {

    if (riderService.deleteRider(id)) {
        return ResponseEntity.noContent().build();
    }

    return ResponseEntity.notFound().build();
}
}
