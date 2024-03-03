package ru.boldyrev.otus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.boldyrev.otus.exception.ConflictException;
import ru.boldyrev.otus.model.dto.rest.*;
import ru.boldyrev.otus.model.entity.Company;
import ru.boldyrev.otus.model.entity.PickupPoint;
import ru.boldyrev.otus.repo.CompanyRepo;
import ru.boldyrev.otus.repo.PickupPointRepo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DeliveryService {
    private final PickupPointRepo pickupPointRepo;

    private final CompanyRepo companyRepo;

    public List<RestPickupPoint> getPickups(String like) {
        List<PickupPoint> pickupPoints;
        if (like == null)
            pickupPoints = pickupPointRepo.findAll();
        else if (like.isEmpty()) {
            pickupPoints = pickupPointRepo.findAll();
        } else {
            pickupPoints = pickupPointRepo.findAllByAddressLike("%" + like + "%");
        }
        return pickupPoints.stream().map(RestPickupPoint::new).toList();
    }

    public List<RestDeliveryOption> getDeliveryOption(String address){
        List<RestDeliveryOption> options = new ArrayList<>();

        for (Company company: companyRepo.findAll()){
            options.add(mockGetOption(company, address));
        }
        return options;
    }

    private RestDeliveryOption mockGetOption(Company company, String address) {
        /*Здесь просто сгенерируем требуемые данные*/
        Random random = new Random();

        double cost = 1000 + (random.nextDouble() * (10000 - 1000));
        cost = Math.round(cost * 100.0) / 100.0;

        int plusDays =  random.nextInt(8) + 1;

        RestDeliveryOption deliveryOption = new RestDeliveryOption();

        deliveryOption.setCompanyName(company.getCompanyName());
        deliveryOption.setAddress(address);
        deliveryOption.setCost(cost);
        deliveryOption.setEstimatedDate(LocalDate.now().plusDays(plusDays));

        return deliveryOption;
    }

    public RestPickupPoint pickupAdjust(RestPickupPoint restPickup) {
        PickupPoint pickupPoint = null;
        if (restPickup.getId() != null) {
            pickupPoint = pickupPointRepo.findById(restPickup.getId()).orElse(null);
        }
        if (pickupPoint == null) {
            pickupPoint = new PickupPoint().setId(restPickup.getId()).setName(restPickup.getName()).setAddress(restPickup.getAddress());
        } else {
            pickupPoint.setAddress(pickupPoint.getAddress()).setName(pickupPoint.getName());
        }
        pickupPoint = pickupPointRepo.saveAndFlush(pickupPoint);
        return new RestPickupPoint(pickupPoint);
    }

    public RestCompany companyAdjust(RestCompany restCompany) throws ConflictException {
        Company company = null;
        if (restCompany.getCompanyName() != null) {
            company = companyRepo.findById(restCompany.getCompanyName()).orElse(null);
        }
        if (company == null) {

            company = new Company().setCompanyName(restCompany.getCompanyName());
        } else {

            company.setCompanyName(restCompany.getCompanyName());
        }
        company = companyRepo.saveAndFlush(company);

        return new RestCompany(company);
    }
}
