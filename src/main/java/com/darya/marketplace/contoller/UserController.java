package com.darya.marketplace.contoller;

import com.darya.marketplace.entity.Basket;
import com.darya.marketplace.entity.Client;
import com.darya.marketplace.entity.Product;
import com.darya.marketplace.entity.Seller;
import com.darya.marketplace.repository.*;
import com.darya.marketplace.security.ClientDetails;
import com.darya.marketplace.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {
    private final ProductRepository productRepository;
    private final ClientRepository clientRepository;
    private final ClientService clientService;
    private final SellerRepository sellerRepository;
    private final ImageRepository imageRepository;
    private final BasketRepository basketRepository;

    @Autowired
    public UserController(ProductRepository productRepository, ClientRepository clientRepository, ClientService clientService, SellerRepository sellerRepository, ImageRepository imageRepository, BasketRepository basketRepository) {
        this.productRepository = productRepository;
        this.clientRepository = clientRepository;
        this.clientService = clientService;
        this.sellerRepository = sellerRepository;
        this.imageRepository = imageRepository;
        this.basketRepository = basketRepository;
    }

    @GetMapping("/hello")
    public String index(){
        return "hello";
    }

    @GetMapping("/")
    public String showInfo(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        ClientDetails clientDetails = (ClientDetails) authentication.getPrincipal();
        System.out.println(clientDetails.getClient().getMoney());

        return "hello";
    }

    @GetMapping("/products")
    public String showProducts(Model model){
        List<Product> products =  productRepository.findAllByStatus(true);
        model.addAttribute("products", products);

        Map<Integer, String> productBase64Images = new HashMap<>();
        for(Product product : products){
            productBase64Images.put(product.getId(), Base64.getEncoder().encodeToString(product.getImages().get(0).getImage()));
        }
        model.addAttribute("images", productBase64Images);

        return "products";
    }

    @GetMapping("/product/{id}")
    public String showProduct(Model model,
                              @PathVariable("id")int id){
        Product product = productRepository.findById(id).orElseThrow(()-> new RuntimeException("product not found"));
        if (!product.isStatus()){
            throw new RuntimeException("product status false");
        }
        Basket basket = new Basket();
        basket.setProduct(product);
        basket.setCount(1);

        model.addAttribute("product", basket);
        return "product";
    }

    @PostMapping("/product/{id}")
    public String addToBasket(@ModelAttribute("basket") Basket basket,
                              @PathVariable("id") int id){
        Product product = productRepository.findById(id).orElseThrow(()-> new RuntimeException("product not found"));

        basket.setClient(getCurrentClient());
        basket.setProduct(product);
        basketRepository.save(basket);

        return "redirect:/products/" + id;
    }

    @GetMapping("/basket")
    public String showBasket(Model model){
        model.addAttribute("baskets", getCurrentClient().getBaskets());
        return "baskets";
    }

    @PostMapping("/basket")
    public String buyProducts(){
        clientService.buyProducts(getCurrentClient());
        return "redirect:/products";
    }

    @PostMapping("/basket/{id}")
    public String deleteFromBasket(@PathVariable("id") int id){
        basketRepository.deleteById(id);
        return "redirect:/basket";
    }

    @GetMapping("/seller/{id}")
    public String showSeller(@PathVariable("id") int id,
                             Model model){
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(()->new RuntimeException("seller with id " + id + " not found"));
        model.addAttribute("seller", seller);

        return "seller";
    }

    private Client getCurrentClient(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        ClientDetails clientDetails = (ClientDetails) authentication.getPrincipal();
        return clientDetails.getClient();
    }
}
