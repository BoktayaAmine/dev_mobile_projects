package gestion_compte.gestion_compte.controllers;

import gestion_compte.gestion_compte.entities.Compte;
import gestion_compte.gestion_compte.entities.CompteRequest;
import gestion_compte.gestion_compte.entities.TypeCompte;
import gestion_compte.gestion_compte.repositories.CompteRepository;
import graphql.language.Argument;
import lombok.AllArgsConstructor;
import org.springframework.expression.ParseException;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.graphql.data.method.annotation.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@AllArgsConstructor
public class CompteControllerGraphQL {

    private CompteRepository compteRepository;
    @QueryMapping
    public List<Compte> allComptes (){
        return compteRepository.findAll();
    }

    @QueryMapping
    public Compte compteById(@org.springframework.graphql.data.method.annotation.Argument  Long id){
        Compte compte = compteRepository.findById(id) .orElse(null) ;
        if (compte == null) throw new RuntimeException (String.format ("Compte %s not found", id));
        else return compte;
    }

    @QueryMapping
    public List<Compte> compteByType(@org.springframework.graphql.data.method.annotation.Argument TypeCompte type) {
        return compteRepository.findByType (type);
    }

    @MutationMapping
    public Compte saveCompte(@org.springframework.graphql.data.method.annotation.Argument("compte") CompteRequest compteRequest) {
        Compte compte = new Compte();
        compte.setSolde(compteRequest.getSolde());

        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = dateFormat.parse(compteRequest.getDateCreation());
            compte.setDateCreation(date);
        } catch (ParseException | java.text.ParseException e) {
            throw new RuntimeException("Invalid date format, expected yyyy-MM-dd");
        }

        compte.setType(compteRequest.getType());
        return compteRepository.save(compte);
    }


    @MutationMapping
    public String deleteCompte(@org.springframework.graphql.data.method.annotation.Argument Long id) {
        if (!compteRepository.existsById(id)) {
            throw new RuntimeException(String.format("Compte %s not found", id));
        }
        compteRepository.deleteById(id);
        return String.format("Compte %s deleted successfully", id);
    }

    @QueryMapping
    public Map<String, Object> totalSolde() {
        long count = compteRepository.count(); // Nombre total de comptes
        double sum = compteRepository.sumSoldes(); // Somme totale des soldes
        double average = count > 0 ? sum / count : 0; // Moyenne des soldes

        return Map.of(
                "count", count,
                "sum", sum,
                "average", average
        );
    }

}

