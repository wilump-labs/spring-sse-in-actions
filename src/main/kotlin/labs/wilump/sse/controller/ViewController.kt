package labs.wilump.sse.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping



@Controller
class ViewController {

    @GetMapping("/")
    fun home(model: Model): String {
        return "index"
    }
}