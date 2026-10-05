from __future__ import annotations

from dataclasses import dataclass
from typing import Dict, List, Optional


@dataclass
class Battery:
    battery_id: str
    capacity_mah: int
    level_percent: int = 100
    assigned_drone_id: Optional[str] = None

    def __post_init__(self) -> None:
        if self.capacity_mah <= 0:
            raise ValueError("capacity_mah must be positive")
        if not 0 <= self.level_percent <= 100:
            raise ValueError("level_percent must be between 0 and 100")


@dataclass
class Drone:
    drone_id: str
    model: str
    assigned_battery_id: Optional[str] = None


class DroneBatteryManagementSystem:
    def __init__(self) -> None:
        self._drones: Dict[str, Drone] = {}
        self._batteries: Dict[str, Battery] = {}

    def register_drone(self, drone_id: str, model: str) -> Drone:
        if drone_id in self._drones:
            raise ValueError(f"Drone already exists: {drone_id}")
        drone = Drone(drone_id=drone_id, model=model)
        self._drones[drone_id] = drone
        return drone

    def register_battery(self, battery_id: str, capacity_mah: int, level_percent: int = 100) -> Battery:
        if battery_id in self._batteries:
            raise ValueError(f"Battery already exists: {battery_id}")
        battery = Battery(
            battery_id=battery_id,
            capacity_mah=capacity_mah,
            level_percent=level_percent,
        )
        self._batteries[battery_id] = battery
        return battery

    def assign_battery_to_drone(self, drone_id: str, battery_id: str) -> None:
        drone = self._get_drone(drone_id)
        battery = self._get_battery(battery_id)

        if drone.assigned_battery_id is not None:
            raise ValueError(f"Drone already has battery: {drone.assigned_battery_id}")
        if battery.assigned_drone_id is not None:
            raise ValueError(f"Battery already assigned to drone: {battery.assigned_drone_id}")

        drone.assigned_battery_id = battery_id
        battery.assigned_drone_id = drone_id

    def release_battery_from_drone(self, drone_id: str) -> None:
        drone = self._get_drone(drone_id)
        if drone.assigned_battery_id is None:
            raise ValueError(f"Drone has no assigned battery: {drone_id}")

        battery = self._get_battery(drone.assigned_battery_id)
        battery.assigned_drone_id = None
        drone.assigned_battery_id = None

    def consume_battery(self, drone_id: str, percent: int) -> int:
        if percent < 0:
            raise ValueError("percent must be non-negative")

        drone = self._get_drone(drone_id)
        if drone.assigned_battery_id is None:
            raise ValueError(f"Drone has no assigned battery: {drone_id}")

        battery = self._get_battery(drone.assigned_battery_id)
        battery.level_percent = max(0, battery.level_percent - percent)
        return battery.level_percent

    def charge_battery(self, battery_id: str, percent: int) -> int:
        if percent < 0:
            raise ValueError("percent must be non-negative")

        battery = self._get_battery(battery_id)
        battery.level_percent = min(100, battery.level_percent + percent)
        return battery.level_percent

    def low_battery_drones(self, threshold_percent: int = 20) -> List[Drone]:
        if not 0 <= threshold_percent <= 100:
            raise ValueError("threshold_percent must be between 0 and 100")

        result: List[Drone] = []
        for drone in self._drones.values():
            if drone.assigned_battery_id is None:
                continue
            battery = self._batteries[drone.assigned_battery_id]
            if battery.level_percent <= threshold_percent:
                result.append(drone)
        return result

    def _get_drone(self, drone_id: str) -> Drone:
        try:
            return self._drones[drone_id]
        except KeyError as exc:
            raise ValueError(f"Unknown drone_id: {drone_id}") from exc

    def _get_battery(self, battery_id: str) -> Battery:
        try:
            return self._batteries[battery_id]
        except KeyError as exc:
            raise ValueError(f"Unknown battery_id: {battery_id}") from exc
