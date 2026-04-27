package in.cg.main.service;

import in.cg.main.dto.PrescriptionReviewRequest;
import in.cg.main.entities.Prescription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminPrescriptionService {

    private static final Logger log = LoggerFactory.getLogger(AdminPrescriptionService.class);

    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;
    private final String catalogServiceBaseUrl;
    private final String orderServiceBaseUrl;

    public AdminPrescriptionService(RestTemplate restTemplate,
                                    DiscoveryClient discoveryClient,
                                    @Value("${catalog.service.url:http://localhost:8082}") String catalogServiceBaseUrl,
                                    @Value("${order.service.url:http://localhost:8083}") String orderServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
        this.catalogServiceBaseUrl = catalogServiceBaseUrl;
        this.orderServiceBaseUrl = orderServiceBaseUrl;
    }

    public List<Prescription> getPendingPrescriptions() {
        for (String baseUrl : candidateBaseUrls(catalogServiceBaseUrl, "CATALOG-SERVICE")) {
            try {
                Prescription[] response = restTemplate.getForObject(
                        baseUrl + "/api/prescriptions/internal/pending",
                        Prescription[].class);
                return response == null ? List.of() : Arrays.asList(response);
            } catch (Exception ignored) {
            }
        }
        throw new RuntimeException("Unable to fetch pending prescriptions");
    }

    public Prescription getPrescriptionById(Long id) {
        for (String baseUrl : candidateBaseUrls(catalogServiceBaseUrl, "CATALOG-SERVICE")) {
            try {
                return restTemplate.getForObject(
                        baseUrl + "/api/prescriptions/internal/" + id,
                        Prescription.class);
            } catch (Exception ignored) {
            }
        }
        throw new RuntimeException("Prescription not found");
    }

    @org.springframework.cache.annotation.CacheEvict(value = "dashboard", allEntries = true)
    public Prescription reviewPrescription(Long prescriptionId,
                                           PrescriptionReviewRequest request,
                                           Long adminId) {
        if (!request.isApproved()
                && (request.getRejectionReason() == null || request.getRejectionReason().isBlank())) {
            throw new RuntimeException("Rejection reason is required");
        }

        Prescription reviewed = null;
        RuntimeException lastFailure = null;

        for (String baseUrl : candidateBaseUrls(catalogServiceBaseUrl, "CATALOG-SERVICE")) {
            try {
                String url = UriComponentsBuilder
                        .fromHttpUrl(baseUrl + "/api/prescriptions/internal/{id}/review")
                        .queryParam("approved", request.isApproved())
                        .queryParam("rejectionReason", request.getRejectionReason())
                        .buildAndExpand(prescriptionId)
                        .encode()
                        .toUriString();

                RequestEntity<Void> entity = new RequestEntity<>(HttpMethod.PUT, URI.create(url));
                ResponseEntity<Prescription> response = restTemplate.exchange(entity, Prescription.class);
                reviewed = response.getBody();
                break;
            } catch (Exception ex) {
                lastFailure = new RuntimeException("Unable to review prescription", ex);
            }
        }

        if (reviewed == null) {
            throw lastFailure != null ? lastFailure : new RuntimeException("Unable to review prescription");
        }

        syncPrescriptionReviewToOrders(reviewed.getId(), request.isApproved());
        reviewed.setReviewedBy(adminId);
        return reviewed;
    }

    private void syncPrescriptionReviewToOrders(Long prescriptionId, boolean approved) {
        for (String baseUrl : candidateBaseUrls(orderServiceBaseUrl, "ORDER-SERVICE")) {
            try {
                String url = UriComponentsBuilder
                        .fromHttpUrl(baseUrl + "/api/internal/orders/prescriptions/{prescriptionId}/sync-status")
                        .queryParam("approved", approved)
                        .buildAndExpand(prescriptionId)
                        .toUriString();

                RequestEntity<Void> entity = new RequestEntity<>(HttpMethod.PUT, URI.create(url));
                restTemplate.exchange(entity, Void.class);
                log.info("Synced prescription {} review to order service via {}", prescriptionId, baseUrl);
                return;
            } catch (Exception ex) {
                log.warn("Failed to sync prescription {} review to order service via {}: {}",
                        prescriptionId, baseUrl, ex.getMessage());
            }
        }
        throw new RuntimeException("Prescription reviewed, but failed to sync order status");
    }

    private List<String> candidateBaseUrls(String configuredUrl, String serviceName) {
        Set<String> urls = new LinkedHashSet<>();
        if (configuredUrl != null && !configuredUrl.isBlank()) {
            urls.add(configuredUrl.replaceAll("/+$", ""));
        }
        if (discoveryClient != null) {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            for (ServiceInstance instance : instances) {
                urls.add(instance.getUri().toString().replaceAll("/+$", ""));
            }
        }
        return new ArrayList<>(urls);
    }
}
