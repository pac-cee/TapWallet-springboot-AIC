package rw.ac.auca.tapwallet.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rw.ac.auca.tapwallet.domain.Merchant;
import rw.ac.auca.tapwallet.dto.MerchantRequest;
import rw.ac.auca.tapwallet.service.MerchantService;

import java.util.List;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Merchant register(@Valid @RequestBody MerchantRequest request) {
        return merchantService.register(request);
    }

    @GetMapping
    public List<Merchant> findAll() {
        return merchantService.findAll();
    }

    @GetMapping("/{id}")
    public Merchant findById(@PathVariable Long id) {
        return merchantService.findById(id);
    }

    @PutMapping("/{id}")
    public Merchant update(@PathVariable Long id, @Valid @RequestBody MerchantRequest request) {
        return merchantService.update(id, request);
    }

    @PatchMapping("/{id}/freeze")
    public Merchant freeze(@PathVariable Long id) {
        return merchantService.freeze(id);
    }

    @PatchMapping("/{id}/activate")
    public Merchant activate(@PathVariable Long id) {
        return merchantService.activate(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        merchantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
