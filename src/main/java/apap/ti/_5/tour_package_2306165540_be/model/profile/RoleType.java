package apap.ti._5.tour_package_2306165540_be.model.profile;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoleType {
    SUPERADMIN("Superadmin", "Mengelola penuh seluruh data dan konfigurasi sistem termasuk user dan vendor."),
    ACCOMMODATION_OWNER("Accommodation Owner",
            "Mengelola layanan penginapan seperti hotel atau vila, termasuk kamar, fasilitas, harga, dan pesanan."),
    FLIGHT_AIRLINE("Flight Airline", "Mengatur jadwal, harga tiket, dan pemrosesan pemesanan layanan penerbangan."),
    INSURANCE_PROVIDER("Insurance Provider", "Mengelola paket asuransi perjalanan dan menangani klaim customer."),
    TOUR_PACKAGE_VENDOR("Tour Package Vendor",
            "Menyediakan serta mengatur paket wisata lengkap meliputi itinerary, harga, dan pemesanan."),
    RENTAL_VENDOR("Rental Vendor",
            "Mengelola layanan penyewaan kendaraan, termasuk data kendaraan, tarif, dan transaksi."),
    CUSTOMER("Customer",
            "Pengguna akhir aplikasi untuk registrasi, login, dan pemesanan layanan tiket, hotel, rental, atau asuransi.");

    private final String displayName;
    private final String responsibility;

    public static RoleType fromCode(String code) {
        for (RoleType value : RoleType.values()) {
            if (value.name().equalsIgnoreCase(code) || value.displayName.equalsIgnoreCase(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown role type: " + code);
    }
}
