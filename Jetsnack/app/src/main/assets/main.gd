extends Node3D

const SnackScene = preload("res://snack.tscn")
const Snack = preload("res://snack.gd")

@onready var control_points: Node3D = %ControlPoints
@onready var snack_parent: Node3D = %Snacks
@onready var animation_player: AnimationPlayer = %AnimationPlayer
@onready var launch_timer: Timer = %LaunchTimer


var app_plugin
var snacks: Array[Snack]
var snack_count := 0


func _ready() -> void:
	animation_player.play(&"idle")

	var points: PackedVector3Array
	var dist: PackedFloat32Array
	for child in control_points.get_children():
		points.push_back(child.global_position)
		dist.push_back(child.max_distance)

	app_plugin = Engine.get_singleton("AppPlugin")
	if app_plugin:
		for fn in app_plugin.getSnackFilenames():
			var texture = load("res://assets/snacks/" + fn)
			snacks.push_back(create_snack(texture))
	else:
		# If we can't get data from the plugin, show a "demo snack".
		snacks.push_back(create_snack(null))

	randomize()

	snack_count = snacks.size()
	for snack in snacks:
		snack.visible = false
		snack.setup_path(points, dist)


func create_snack(p_texture: Texture2D) -> Snack:
	var snack = SnackScene.instantiate()
	snack_parent.add_child(snack)
	if p_texture:
		snack.set_texture(p_texture)
	snack.finished_flying.connect(_on_snack_finished_flying)
	return snack


func _on_start_timer_timeout() -> void:
	for snack in snacks:
		snack.visible = true
		snack.start_flying()
		await get_tree().create_timer(randf_range(0.2, 0.4)).timeout


func _on_snack_finished_flying() -> void:
	if animation_player.is_playing() and animation_player.current_animation == &"bonk":
		animation_player.stop()
	animation_player.play(&"bonk")

	snack_count -= 1
	if snack_count == 0:
		launch_timer.start()


func _on_launch_timer_timeout() -> void:
	animation_player.play(&"launch")


func _on_animation_player_animation_finished(p_anim_name: StringName) -> void:
	if p_anim_name == &"bonk":
		# After "bonk" return to "idle".
		animation_player.play(&"idle")
	if p_anim_name == &"launch":
		if app_plugin:
			app_plugin.goBack()
