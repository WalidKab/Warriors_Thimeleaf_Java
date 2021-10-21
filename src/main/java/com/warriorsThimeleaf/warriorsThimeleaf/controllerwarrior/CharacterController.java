package com.warriorsThimeleaf.warriorsThimeleaf.controllerwarrior;

import java.util.ArrayList;
import java.util.List;

import com.warriorsThimeleaf.warriorsThimeleaf.form.CharacterForm;
import com.warriorsThimeleaf.warriorsThimeleaf.model.CharacterModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Controller
public class CharacterController {
    private static List<CharacterModel> characters = new ArrayList<CharacterModel>();

    static {
        characters.add(new CharacterModel(1, "Mage noir", "Magicien"));
        characters.add(new CharacterModel(2, "Mage blanc", "Magicien"));
        characters.add(new CharacterModel(3, "Guerrier nordique", "Guerrier"));
    }

     //Injectez (inject) via application.properties.
    @Value("${welcome.message}")
    private String message;

    @Value("${error.message}")
    private String errorMessage;

    @RequestMapping(value = { "/", "/index" }, method = RequestMethod.GET)
    public String index(Model model) {

        model.addAttribute("message", message);

        return "index";
    }

    @RequestMapping(value = { "/characterList" }, method = RequestMethod.GET)
    public String characterList(Model model) {
        List<CharacterModel> characters =  new RestTemplate().getForObject("http://localhost:8080/Character", List.class);

        model.addAttribute("characters", characters);

        return "characterList";
    }

    @RequestMapping(value = { "/addCharacter" }, method = RequestMethod.GET)
    public String showAddCharacterPage(Model model) {

        CharacterForm characterForm = new CharacterForm();
        model.addAttribute("characterForm", characterForm);

        return "addCharacter";
    }

    @RequestMapping(value = { "/addCharacter" }, method = RequestMethod.POST)
    public String savePerson(Model model, //
                             @ModelAttribute("characterForm") CharacterForm characterForm) {
        List<CharacterModel> characters =  new RestTemplate().getForObject("http://localhost:8080/Character", List.class);

        int id = characters.size();
        String name = characterForm.getName();
        String type = characterForm.getType();

        if (name != null && name.length() > 0 //
                && type != null && type.length() > 0) {
            CharacterModel newCharacter = new CharacterModel(id, name, type);
            new RestTemplate().postForObject("http://localhost:8080/Character", newCharacter, CharacterModel.class);

//            characters.add(newCharacter);

            return "redirect:/characterList";
        }

        model.addAttribute("errorMessage", errorMessage);
        return "addCharacter";
    }

    @RequestMapping(value = "/deleteUser/{id}", method = RequestMethod.GET)
    public String deleteById(@PathVariable int id) {

        new RestTemplate().delete("http://localhost:8080/Character/" + id);

        return "redirect:/characterList";
    }

    @RequestMapping(value = "/editCharacter/{id}", method = RequestMethod.GET)
    public String editById(Model model, @PathVariable int id) {
        CharacterModel[] characters =  new RestTemplate().getForObject("http://localhost:8080/Character", CharacterModel[].class);

        for (CharacterModel character : characters){
            if (character.getId() == id){
                CharacterForm characterForm = new CharacterForm(character.getId(), character.getName(), character.getType());
                model.addAttribute("characterForm", characterForm);
            }
        }
        return "editCharacter" ;
    }

    @RequestMapping(value = { "/editCharacter/{id}" }, method = RequestMethod.POST)
    public String saveEdit(@ModelAttribute("characterForm") CharacterForm characterForm, @PathVariable int id) {
        CharacterModel[] characters =  new RestTemplate().getForObject("http://localhost:8080/Character", CharacterModel[].class);

        for (CharacterModel character : characters){
            if (character.getId() == id){
                character.setName(characterForm.getName());
                character.setType(characterForm.getType());
                new RestTemplate().put("http://localhost:8080/Character/" + character.getId(), character, CharacterModel.class);
            }
        }
        return "redirect:/characterList";
    }
}