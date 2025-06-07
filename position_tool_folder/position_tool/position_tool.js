const x_origin_input = document.getElementById('x_origin_input')
const y_origin_input = document.getElementById('y_origin_input')
const update_origin_button = document.getElementById('update_origin')

const floor_input = document.getElementById('floor_input')
const update_floor_button = document.getElementById('update_floor')

const current_floor_p = document.getElementById('current_floor')
const current_origin_p = document.getElementById('current_origin')
const raw_position_p = document.getElementById('position_raw')
const calculated_position_p = document.getElementById('position_calculated')

const origin_location = document.getElementById('origin_location')
const position_location = document.getElementById('position_location')
const position_location_label = document.getElementById('position_location_label')

const floor_img = document.getElementById('floor_img')

let origin_x = 0
let origin_y = 0

let calculated_x = 0
let calculated_y = 0

let raw_position_x = 0
let raw_position_y = 0

function printMousePos(event) {
    raw_position_p.innerHTML = "Raw Position | X: " + event.pageX + ", Y: " + event.pageY;
    
    calculated_x = event.pageX - origin_x
    calculated_y = origin_y - event.pageY

    raw_position_x = event.pageX
    raw_position_y = event.pageY
    
    updatePosition()
}

function updatePosition() {
    let s = "Calculated Position |  X: " + calculated_x + ", Y: " + calculated_y

    calculated_position_p.innerHTML = s
    position_location_label.innerHTML = s

    let square_bounding = position_location.getBoundingClientRect()
    position_location.style.left = raw_position_x - (square_bounding.width / 2)
    position_location.style.top = raw_position_y - (square_bounding.height / 2)

    let bounding_label = position_location_label.getBoundingClientRect()
    position_location_label.style.left = raw_position_x - (bounding_label.width / 2)
    position_location_label.style.top = raw_position_y - (bounding_label.height / 2) - (square_bounding.height * 4)
}

function updateOrigin() {
    origin_x = parseInt(x_origin_input.value)
    origin_y = parseInt(y_origin_input.value)
    current_origin_p.innerHTML = "Current Origin | X: " + origin_x + ", Y: " + origin_y
    
    let bounding = origin_location.getBoundingClientRect()
    origin_location.style.left = origin_x - (bounding.width / 2)
    origin_location.style.top = origin_y - (bounding.height / 2)
}

function updateFloor() {
    floor_img.src = "../floor " + floor_input.value + " 1536x1536.png"
    current_floor_p.innerHTML = "Current Floor | " + floor_input.value
}

document.addEventListener("click", printMousePos)

update_origin_button.onclick=updateOrigin
update_floor_button.onclick=updateFloor

document.addEventListener("keypress", function(event){
    let key = event.key

    console.log(key)

    if (key == "w"){
        calculated_y += 1
        raw_position_y -= 1
    } else if (key == "s") {
        calculated_y -= 1
        raw_position_y += 1 
    }
    
    if (key == "a") {
        calculated_x -= 1
        raw_position_x -= 1
    }else if (key == "d") {
        calculated_x += 1
        raw_position_x += 1
    }

    updatePosition()
})