package com.moviefixer.analytics.repository;

import com.moviefixer.analytics.dto.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Repository
public class AnalyticsRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public PlatformOverviewResponse getPlatformOverview() {
        // 1. Financial and booking counts
        String bookingSql = """
            SELECT
                COALESCE(SUM(CASE WHEN status = 'CONFIRMED' THEN net_amount ELSE 0 END), 0) AS gross_revenue,
                COALESCE(SUM(CASE WHEN status = 'CONFIRMED' THEN convenience_fee ELSE 0 END), 0) AS convenience_fee,
                COUNT(id) AS total_bookings,
                COUNT(CASE WHEN status = 'CONFIRMED' THEN 1 END) AS confirmed_bookings,
                COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) AS cancelled_bookings
            FROM bookings
            """;

        Object[] bookingRow = (Object[]) entityManager.createNativeQuery(bookingSql).getSingleResult();
        BigDecimal grossRevenue = toBigDecimal(bookingRow[0]);
        BigDecimal convenienceFee = toBigDecimal(bookingRow[1]);
        BigDecimal vendorPayout = grossRevenue.subtract(convenienceFee);
        long totalBookings = toLong(bookingRow[2]);
        long confirmedBookings = toLong(bookingRow[3]);
        long cancelledBookings = toLong(bookingRow[4]);

        double cancellationRate = (totalBookings > 0)
                ? BigDecimal.valueOf((double) cancelledBookings / totalBookings * 100.0)
                .setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        // 2. Tickets sold
        String ticketSql = """
            SELECT COUNT(t.id)
            FROM tickets t
            JOIN bookings b ON t.booking_id = b.id
            WHERE b.status = 'CONFIRMED'
            """;
        long totalTicketsSold = toLong(entityManager.createNativeQuery(ticketSql).getSingleResult());

        // 3. Platform assets
        long totalTheaters = toLong(entityManager.createNativeQuery("SELECT COUNT(id) FROM theaters WHERE is_active = true").getSingleResult());
        long totalScreens = toLong(entityManager.createNativeQuery("SELECT COUNT(id) FROM screens WHERE is_active = true").getSingleResult());
        long totalShows = toLong(entityManager.createNativeQuery("SELECT COUNT(id) FROM shows").getSingleResult());

        BigDecimal averageTicketPrice = (totalTicketsSold > 0)
                ? grossRevenue.divide(BigDecimal.valueOf(totalTicketsSold), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new PlatformOverviewResponse(
                grossRevenue, convenienceFee, vendorPayout, totalBookings,
                confirmedBookings, cancelledBookings, cancellationRate, totalTicketsSold,
                totalTheaters, totalScreens, totalShows, averageTicketPrice
        );
    }

    @SuppressWarnings("unchecked")
    public List<TopMovieAnalyticsResponse> getTopMovies(int limit) {
        String sql = """
            SELECT
                m.id AS movie_id,
                m.title AS movie_title,
                COALESCE(m.poster_url, '') AS poster_url,
                COALESCE(l.name, 'N/A') AS language,
                COUNT(DISTINCT s.id) AS total_shows,
                COUNT(t.id) AS tickets_sold,
                COALESCE(SUM(b.net_amount), 0) AS gross_revenue,
                COALESCE(SUM(sc.total_seats), 0) AS total_capacity
            FROM movies m
            LEFT JOIN languages l ON m.language_id = l.id
            JOIN shows s ON s.movie_id = m.id
            JOIN screens sc ON s.screen_id = sc.id
            LEFT JOIN bookings b ON b.show_id = s.id AND b.status = 'CONFIRMED'
            LEFT JOIN tickets t ON t.booking_id = b.id
            GROUP BY m.id, m.title, m.poster_url, l.name
            ORDER BY gross_revenue DESC, tickets_sold DESC
            LIMIT :limit
            """;

        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("limit", limit)
                .getResultList();

        List<TopMovieAnalyticsResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            UUID movieId = toUUID(row[0]);
            String title = (String) row[1];
            String posterUrl = (String) row[2];
            String language = (String) row[3];
            long totalShows = toLong(row[4]);
            long ticketsSold = toLong(row[5]);
            BigDecimal grossRevenue = toBigDecimal(row[6]);
            long totalCapacity = toLong(row[7]);

            double occupancyRate = (totalCapacity > 0)
                    ? BigDecimal.valueOf((double) ticketsSold / totalCapacity * 100.0)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;

            result.add(new TopMovieAnalyticsResponse(
                    movieId, title, posterUrl, language, totalShows,
                    ticketsSold, grossRevenue, occupancyRate
            ));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<CityAnalyticsResponse> getCityAnalytics() {
        String sql = """
            SELECT
                c.id AS city_id,
                c.name AS city_name,
                st.name AS state_name,
                COUNT(DISTINCT th.id) AS theaters_count,
                COUNT(DISTINCT sc.id) AS screens_count,
                COUNT(DISTINCT sh.id) AS shows_count,
                COUNT(t.id) AS tickets_sold,
                COALESCE(SUM(b.net_amount), 0) AS gross_revenue
            FROM cities c
            JOIN states st ON c.state_id = st.id
            LEFT JOIN theaters th ON th.city_id = c.id AND th.is_active = true
            LEFT JOIN screens sc ON sc.theater_id = th.id AND sc.is_active = true
            LEFT JOIN shows sh ON sh.screen_id = sc.id
            LEFT JOIN bookings b ON b.show_id = sh.id AND b.status = 'CONFIRMED'
            LEFT JOIN tickets t ON t.booking_id = b.id
            GROUP BY c.id, c.name, st.name
            ORDER BY gross_revenue DESC, theaters_count DESC
            """;

        List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();
        List<CityAnalyticsResponse> result = new ArrayList<>();

        for (Object[] row : rows) {
            Long cityId = toLong(row[0]);
            String cityName = (String) row[1];
            String stateName = (String) row[2];
            long theatersCount = toLong(row[3]);
            long screensCount = toLong(row[4]);
            long showsCount = toLong(row[5]);
            long ticketsSold = toLong(row[6]);
            BigDecimal grossRevenue = toBigDecimal(row[7]);

            result.add(new CityAnalyticsResponse(
                    cityId, cityName, stateName, theatersCount, screensCount,
                    showsCount, ticketsSold, grossRevenue
            ));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<DailyRevenueTrendResponse> getDailyRevenueTrend(int days) {
        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);

        String sql = """
            SELECT
                CAST(b.created_at AT TIME ZONE 'UTC' AS DATE) AS booking_date,
                COUNT(DISTINCT b.id) AS booking_count,
                COUNT(t.id) AS tickets_sold,
                COALESCE(SUM(b.net_amount), 0) AS gross_revenue,
                COALESCE(SUM(b.convenience_fee), 0) AS convenience_fee
            FROM bookings b
            LEFT JOIN tickets t ON t.booking_id = b.id
            WHERE b.status = 'CONFIRMED' AND b.created_at >= :cutoff
            GROUP BY CAST(b.created_at AT TIME ZONE 'UTC' AS DATE)
            ORDER BY booking_date ASC
            """;

        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("cutoff", cutoff)
                .getResultList();

        List<DailyRevenueTrendResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            String dateStr = String.valueOf(row[0]);
            long bookingCount = toLong(row[1]);
            long ticketsSold = toLong(row[2]);
            BigDecimal grossRevenue = toBigDecimal(row[3]);
            BigDecimal convenienceFee = toBigDecimal(row[4]);

            result.add(new DailyRevenueTrendResponse(
                    dateStr, bookingCount, ticketsSold, grossRevenue, convenienceFee
            ));
        }
        return result;
    }

    public VendorOverviewResponse getVendorOverview(UUID vendorId, String vendorName) {
        // 1. Vendor asset counts
        String assetSql = """
            SELECT
                COUNT(DISTINCT th.id) AS total_theaters,
                COUNT(DISTINCT sc.id) AS total_screens,
                COUNT(DISTINCT sh.id) AS total_shows,
                COALESCE(SUM(sc.total_seats), 0) AS total_capacity
            FROM theaters th
            LEFT JOIN screens sc ON sc.theater_id = th.id AND sc.is_active = true
            LEFT JOIN shows sh ON sh.screen_id = sc.id
            WHERE th.vendor_id = :vendorId AND th.is_active = true
            """;

        Object[] assetRow = (Object[]) entityManager.createNativeQuery(assetSql)
                .setParameter("vendorId", vendorId)
                .getSingleResult();

        long totalTheaters = toLong(assetRow[0]);
        long totalScreens = toLong(assetRow[1]);
        long totalShows = toLong(assetRow[2]);
        long totalCapacity = toLong(assetRow[3]);

        // 2. Vendor financial & ticket counts
        String salesSql = """
            SELECT
                COUNT(DISTINCT t.id) AS tickets_sold,
                COALESCE(SUM(b.net_amount), 0) AS gross_revenue,
                COALESCE(SUM(b.convenience_fee), 0) AS convenience_fee,
                COUNT(CASE WHEN t.is_checked_in = true THEN 1 END) AS admissions_count
            FROM theaters th
            JOIN screens sc ON sc.theater_id = th.id
            JOIN shows sh ON sh.screen_id = sc.id
            JOIN bookings b ON b.show_id = sh.id AND b.status = 'CONFIRMED'
            JOIN tickets t ON t.booking_id = b.id
            WHERE th.vendor_id = :vendorId
            """;

        Object[] salesRow = (Object[]) entityManager.createNativeQuery(salesSql)
                .setParameter("vendorId", vendorId)
                .getSingleResult();

        long ticketsSold = toLong(salesRow[0]);
        BigDecimal grossRevenue = toBigDecimal(salesRow[1]);
        BigDecimal platformFee = toBigDecimal(salesRow[2]);
        BigDecimal netEarnings = grossRevenue.subtract(platformFee);
        long admissionsCount = toLong(salesRow[3]);

        double gateCheckInRate = (ticketsSold > 0)
                ? BigDecimal.valueOf((double) admissionsCount / ticketsSold * 100.0)
                .setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        double overallOccupancy = (totalCapacity > 0)
                ? BigDecimal.valueOf((double) ticketsSold / totalCapacity * 100.0)
                .setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        return new VendorOverviewResponse(
                vendorId, vendorName, totalTheaters, totalScreens, totalShows,
                ticketsSold, grossRevenue, platformFee, netEarnings,
                admissionsCount, gateCheckInRate, overallOccupancy
        );
    }

    @SuppressWarnings("unchecked")
    public List<VendorTheaterAnalyticsResponse> getVendorTheaters(UUID vendorId) {
        String sql = """
            SELECT
                th.id AS theater_id,
                th.name AS theater_name,
                c.name AS city_name,
                COUNT(DISTINCT sc.id) AS screens_count,
                COUNT(DISTINCT sh.id) AS shows_count,
                COUNT(t.id) AS tickets_sold,
                COALESCE(SUM(b.net_amount), 0) AS gross_revenue,
                COALESCE(SUM(sc.total_seats), 0) AS total_capacity
            FROM theaters th
            JOIN cities c ON th.city_id = c.id
            LEFT JOIN screens sc ON sc.theater_id = th.id AND sc.is_active = true
            LEFT JOIN shows sh ON sh.screen_id = sc.id
            LEFT JOIN bookings b ON b.show_id = sh.id AND b.status = 'CONFIRMED'
            LEFT JOIN tickets t ON t.booking_id = b.id
            WHERE th.vendor_id = :vendorId AND th.is_active = true
            GROUP BY th.id, th.name, c.name
            ORDER BY gross_revenue DESC, tickets_sold DESC
            """;

        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("vendorId", vendorId)
                .getResultList();

        List<VendorTheaterAnalyticsResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            UUID theaterId = toUUID(row[0]);
            String theaterName = (String) row[1];
            String cityName = (String) row[2];
            long screensCount = toLong(row[3]);
            long showsCount = toLong(row[4]);
            long ticketsSold = toLong(row[5]);
            BigDecimal grossRevenue = toBigDecimal(row[6]);
            long totalCapacity = toLong(row[7]);

            double occupancyRate = (totalCapacity > 0)
                    ? BigDecimal.valueOf((double) ticketsSold / totalCapacity * 100.0)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;

            result.add(new VendorTheaterAnalyticsResponse(
                    theaterId, theaterName, cityName, screensCount, showsCount,
                    ticketsSold, grossRevenue, occupancyRate
            ));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<VendorMovieAnalyticsResponse> getVendorMovies(UUID vendorId) {
        String sql = """
            SELECT
                m.id AS movie_id,
                m.title AS movie_title,
                COALESCE(l.name, 'N/A') AS language,
                COUNT(DISTINCT sh.id) AS shows_count,
                COUNT(t.id) AS tickets_sold,
                COALESCE(SUM(b.net_amount), 0) AS gross_revenue,
                COALESCE(SUM(sc.total_seats), 0) AS total_capacity
            FROM theaters th
            JOIN screens sc ON sc.theater_id = th.id
            JOIN shows sh ON sh.screen_id = sc.id
            JOIN movies m ON sh.movie_id = m.id
            LEFT JOIN languages l ON m.language_id = l.id
            LEFT JOIN bookings b ON b.show_id = sh.id AND b.status = 'CONFIRMED'
            LEFT JOIN tickets t ON t.booking_id = b.id
            WHERE th.vendor_id = :vendorId
            GROUP BY m.id, m.title, l.name
            ORDER BY gross_revenue DESC, tickets_sold DESC
            """;

        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("vendorId", vendorId)
                .getResultList();

        List<VendorMovieAnalyticsResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            UUID movieId = toUUID(row[0]);
            String movieTitle = (String) row[1];
            String language = (String) row[2];
            long showsCount = toLong(row[3]);
            long ticketsSold = toLong(row[4]);
            BigDecimal grossRevenue = toBigDecimal(row[5]);
            long totalCapacity = toLong(row[6]);

            double occupancyRate = (totalCapacity > 0)
                    ? BigDecimal.valueOf((double) ticketsSold / totalCapacity * 100.0)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;

            result.add(new VendorMovieAnalyticsResponse(
                    movieId, movieTitle, language, showsCount,
                    ticketsSold, grossRevenue, occupancyRate
            ));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<TierBreakdownResponse> getShowTierBreakdowns(UUID showId) {
        String sql = """
            SELECT
                st.name AS tier_name,
                st.tier_type AS tier_type,
                ss.price AS unit_price,
                COUNT(ss.id) AS total_seats,
                COUNT(CASE WHEN ss.status = 'BOOKED' THEN 1 END) AS booked_seats,
                COALESCE(SUM(CASE WHEN ss.status = 'BOOKED' THEN ss.price ELSE 0 END), 0) AS revenue
            FROM show_seats ss
            JOIN seating_tiers st ON ss.tier_id = st.id
            WHERE ss.show_id = :showId
            GROUP BY st.name, st.tier_type, ss.price
            ORDER BY ss.price DESC
            """;

        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("showId", showId)
                .getResultList();

        List<TierBreakdownResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            String tierName = (String) row[0];
            String tierType = String.valueOf(row[1]);
            BigDecimal unitPrice = toBigDecimal(row[2]);
            long totalSeats = toLong(row[3]);
            long bookedSeats = toLong(row[4]);
            BigDecimal revenue = toBigDecimal(row[5]);

            double occupancyPercentage = (totalSeats > 0)
                    ? BigDecimal.valueOf((double) bookedSeats / totalSeats * 100.0)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;

            result.add(new TierBreakdownResponse(
                    tierName, tierType, unitPrice, totalSeats,
                    bookedSeats, occupancyPercentage, revenue
            ));
        }
        return result;
    }

    // Helper conversion methods
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number num) return BigDecimal.valueOf(num.doubleValue()).setScale(2, RoundingMode.HALF_UP);
        return new BigDecimal(value.toString());
    }

    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number num) return num.longValue();
        return Long.parseLong(value.toString());
    }

    private UUID toUUID(Object value) {
        if (value == null) return null;
        if (value instanceof UUID u) return u;
        return UUID.fromString(value.toString());
    }
}
