package com.darya.marketplace.service;

import com.darya.marketplace.entity.Basket;
import com.darya.marketplace.entity.Client;
import com.darya.marketplace.repository.BasketRepository;
import com.darya.marketplace.repository.ClientRepository;
import com.darya.marketplace.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class ClientService {
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final BasketRepository basketRepository;

    @Autowired
    public ClientService(ClientRepository clientRepository,
                         ProductRepository productRepository,
                         BasketRepository basketRepository){
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
        this.basketRepository = basketRepository;
    }

    public Client registration(Client client){
        return clientRepository.save(client);
    }

    @Transactional
    public void buyProducts(Client client){
        double moneyCount = client.getBaskets().stream().mapToDouble(e -> e.getCount() * e.getProduct().getPrice()).sum();

        if (moneyCount > client.getMoney()){
            throw new RuntimeException("Money not have");
        }

        List<Basket> baskets = client.getBaskets();
        for (Basket e : baskets){
            e.getProduct().setCount(e.getProduct().getCount() - e.getCount());
            productRepository.save(e.getProduct());
            basketRepository.delete(e);
        }
        client.setMoney(client.getMoney()-moneyCount);
        clientRepository.save(client);
    }
}
