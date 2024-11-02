package ma.ensa.project.controller;

import ma.ensa.project.model.Position;
import ma.ensa.project.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/positions")
public class PositionController {

    private final PositionService positionService;

    @Autowired
    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping
    public List<Position> getAllPositions() {
        return positionService.getAllPositions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Position> getPositionById(@PathVariable Long id) {
        Optional<Position> position = positionService.getPositionById(id);
        return position.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Position> createPosition(@RequestBody Position position) {
        Position savedPosition = positionService.savePosition(position);
        return new ResponseEntity<>(savedPosition, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Position> updatePosition(@PathVariable Long id, @RequestBody Position positionDetails) {
        Position updatedPosition = positionService.updatePosition(id, positionDetails);
        return ResponseEntity.ok(updatedPosition);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePosition(@PathVariable Long id) {
        positionService.deletePosition(id);
        return ResponseEntity.noContent().build();
    }
}
