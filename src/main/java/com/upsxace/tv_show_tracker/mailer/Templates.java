package com.upsxace.tv_show_tracker.mailer;

import com.upsxace.tv_show_tracker.mailer.dto.TvShowRecommendation;

import java.util.List;

public class Templates {
    public static String recommendationEmail(List<TvShowRecommendation> tvShows){
        // Build HTML content
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family: Arial, sans-serif;'>");
        html.append("<h2>Teker Tv Show Recommendations</h2>");
        html.append("<table style='width: 100%; border-collapse: collapse;'>");

        for (var movie : tvShows) {
            html.append("<tr style='margin-bottom: 20px;'>");
            // Movie poster
            html.append("<td style='padding: 10px; width: 120px;'><img src='")
                    .append(movie.getPosterUrl())
                    .append("' alt='")
                    .append(movie.getName())
                    .append("' style='width: 100px; height: auto;'/></td>");
            // Movie name as clickable link
            html.append("<td style='padding: 10px;'><a href='")
                    .append(movie.getLink())
                    .append("' style='text-decoration: none; color: #2a7ae2; font-size: 16px;'>")
                    .append(movie.getName())
                    .append("</a></td>");
            html.append("</tr>");
        }

        html.append("</table>");
        html.append("</body></html>");

        return html.toString();
    }
}
