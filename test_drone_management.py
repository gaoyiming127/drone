import unittest

from drone_management import DroneBatteryManagementSystem


class DroneBatteryManagementSystemTest(unittest.TestCase):
    def setUp(self) -> None:
        self.system = DroneBatteryManagementSystem()

    def test_register_and_assign_battery(self) -> None:
        self.system.register_drone("d-1", "Mavic")
        self.system.register_battery("b-1", 5000)

        self.system.assign_battery_to_drone("d-1", "b-1")
        low = self.system.low_battery_drones()

        self.assertEqual(low, [])

    def test_consume_and_low_battery_report(self) -> None:
        self.system.register_drone("d-1", "Mavic")
        self.system.register_battery("b-1", 5000, level_percent=30)
        self.system.assign_battery_to_drone("d-1", "b-1")

        self.system.consume_battery("d-1", 15)
        low = self.system.low_battery_drones(threshold_percent=20)

        self.assertEqual([d.drone_id for d in low], ["d-1"])

    def test_charge_caps_at_100(self) -> None:
        self.system.register_battery("b-1", 5000, level_percent=95)

        level = self.system.charge_battery("b-1", 20)

        self.assertEqual(level, 100)

    def test_reject_duplicate_ids(self) -> None:
        self.system.register_drone("d-1", "Mavic")
        self.system.register_battery("b-1", 5000)

        with self.assertRaises(ValueError):
            self.system.register_drone("d-1", "Mini")
        with self.assertRaises(ValueError):
            self.system.register_battery("b-1", 6000)

    def test_reject_assign_when_already_bound(self) -> None:
        self.system.register_drone("d-1", "Mavic")
        self.system.register_drone("d-2", "Mini")
        self.system.register_battery("b-1", 5000)
        self.system.register_battery("b-2", 4500)

        self.system.assign_battery_to_drone("d-1", "b-1")

        with self.assertRaises(ValueError):
            self.system.assign_battery_to_drone("d-1", "b-2")
        with self.assertRaises(ValueError):
            self.system.assign_battery_to_drone("d-2", "b-1")


if __name__ == "__main__":
    unittest.main()
