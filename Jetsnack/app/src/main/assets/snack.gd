extends Node3D

@onready var path: Path3D = %Path3D
@onready var path_follow: PathFollow3D = %PathFollow3D
@onready var sprite: Sprite3D = %Sprite3D
@onready var animation_player: AnimationPlayer = %AnimationPlayer

signal finished_flying()


func set_texture(p_texture: Texture2D) -> void:
	sprite.texture = p_texture

	# Ensure the sprite is no bigger than 1.0 on either dimension.
	var dim: float = maxf(p_texture.get_width(), p_texture.get_height())
	sprite.pixel_size = 1.0 / dim


func setup_path(p_control_points: PackedVector3Array, p_distances: PackedFloat32Array) -> void:
	assert(p_control_points.size() == p_distances.size())

	var points: PackedVector3Array
	for i in p_control_points.size():
		points.push_back(random_point_in_sphere(p_control_points[i], p_distances[i]))

	path.curve = make_smooth_curve(points)


func start_flying() -> void:
	animation_player.play(&"fly")


func _on_animation_player_animation_finished(_anim_name: StringName) -> void:
	finished_flying.emit()


static func random_point_in_sphere(p_center: Vector3, p_max_distance: float) -> Vector3:
	var offset: Vector3

	while true:
		offset = Vector3(
			randf_range(-p_max_distance, p_max_distance),
			randf_range(-p_max_distance, p_max_distance),
			randf_range(-p_max_distance, p_max_distance))

		if offset.length_squared() <= p_max_distance * p_max_distance:
			break

	return p_center + offset


static func make_smooth_curve(p_points: PackedVector3Array, p_handle_factor := 0.25) -> Curve3D:
	var curve := Curve3D.new()
	curve.bake_interval = 0.05

	for i in p_points.size():
		var p := p_points[i]

		var prev: Vector3 = p_points[i - 1] if i > 0 else p
		var next: Vector3 = p_points[i + 1] if i < p_points.size() - 1 else p

		var tangent := next - prev

		var in_handle := Vector3.ZERO
		var out_handle := Vector3.ZERO

		if tangent.length() > 0.001:
			var dir := tangent.normalized()
			if i > 0:
				in_handle = -dir * p.distance_to(prev) * p_handle_factor
			if i < p_points.size() - 1:
				out_handle = dir * p.distance_to(next) * p_handle_factor

		curve.add_point(p, in_handle, out_handle)

	return curve
