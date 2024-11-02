package ma.ensa.project.service;


import ma.ensa.project.model.Position;
import ma.ensa.project.repo.PositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PositionService {

    private final PositionRepository positionRepository;

    @Autowired
    public PositionService(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    public List<Position> getAllPositions() {
        return positionRepository.findAll();
    }

    public Optional<Position> getPositionById(Long id) {
        return positionRepository.findById(id);
    }

    public Position savePosition(Position position) {
        return positionRepository.save(position);
    }

    public Position updatePosition(Long id, Position positionDetails) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Position not found"));

        position.setLatitude(positionDetails.getLatitude());
        position.setLongitude(positionDetails.getLongitude());
        position.setDate(positionDetails.getDate());
        position.setImei(positionDetails.getImei());

        return positionRepository.save(position);
    }

    public void deletePosition(Long id) {
        positionRepository.deleteById(id);
    }
}
