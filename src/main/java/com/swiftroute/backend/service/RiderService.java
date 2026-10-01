package com.swiftroute.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.swiftroute.backend.model.Rider;
import com.swiftroute.backend.repository.RiderRepository;

@Service 
public class RiderService {
private final RiderRepository riderRepository;

public RiderService(RiderRepository riderRepository){
    this.riderRepository = riderRepository;
}
    public Rider saveRider(Rider rider) {
    return riderRepository.save(rider);
}
public List<Rider> getAllRiders() {
    return riderRepository.findAll();
}
public Optional<Rider> getRiderById(Long id) {
    return riderRepository.findById(id);
}
public Optional<Rider> updateRider(Long id, Rider updatedRider) {

    return riderRepository.findById(id)
            .map(existingRider -> {

                existingRider.setName(updatedRider.getName());
                existingRider.setEmail(updatedRider.getEmail());
                existingRider.setPhone(updatedRider.getPhone());
                existingRider.setProfilePhotoUrl(updatedRider.getProfilePhotoUrl());
                existingRider.setStatus(updatedRider.getStatus());
                existingRider.setPreferredVehicle(updatedRider.getPreferredVehicle());

                return riderRepository.save(existingRider);
            });
}
public boolean deleteRider(Long id) {

    if (riderRepository.existsById(id)) {
        riderRepository.deleteById(id);
        return true;
    }

    return false;
}

}

