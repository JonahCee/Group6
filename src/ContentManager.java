

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;


public class ContentManager {
    private static ContentManager instance;
    private List<Content> inventory;
    private Set<String> categories;


    private ContentManager() {
        this.inventory = new ArrayList<>();
        this.categories = new HashSet<>();
        readFromFile(Paths.get("src/Content"));
    }

    public static ContentManager getInstance() {
        if (instance == null) {
            instance = new ContentManager();
        }
        return instance;
    }

    public List<Content> getInventory() {
        return inventory;
    }

    public void borrowContent(int contentID, Customer customer) {

        Content contentToBorrow = this.getContentById(contentID);

        if (contentToBorrow.isAvailable()) {
            contentToBorrow.setAvailable(false);

            System.out.println(customer.getName() + " has successfully borrowed: " + contentToBorrow.getTitle());
        } else {
            System.out.println("The content is not available for borrowing.");
        }


        Rental rental = new Rental(contentToBorrow, customer, LocalDate.now(), false);

        customer.addRental(rental);

    }


    public void readFromFile(Path readDataFromFile) {

        try (Scanner scanner = new Scanner(readDataFromFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.trim().split("\\|");
                int contentID = Integer.parseInt(parts[0]);
                String contentType = parts[1];
                String title = parts[2];
                String director = parts[3];
                String description = parts[4];
                List<String> genres = Arrays.asList(parts[5].split(";"));
                int releaseYear = Integer.parseInt(parts[6]);
                boolean isAvailable = Boolean.parseBoolean(parts[7]);

                for (String genre : genres) {
                    this.categories.add(genre.trim());
                }
                
                if (contentType.equals("Movie")) {
                    int runtTime = Integer.parseInt(parts[8]);
                    boolean hasCreditScenes = Boolean.parseBoolean(parts[9]);
                    Movie movie = new Movie(contentID, title, director, description, releaseYear, isAvailable, genres, runtTime, hasCreditScenes);
                    inventory.add(movie);
                } else if (contentType.equals("Series")) {
                    int totalEpisodes = Integer.parseInt(parts[8]);
                    String[] episodeEachSeason = parts[9].split(",");

                    HashMap<Integer, Integer> episodeEachSeasonMap = new HashMap();
                    for (String pair : episodeEachSeason) {
                        String[] keyValue = pair.split("=");
                        int key = Integer.parseInt(keyValue[0]);
                        int value = Integer.parseInt(keyValue[1]);
                        episodeEachSeasonMap.put(key, value);
                    }

                    Series series = new Series(contentID, title, director, description, releaseYear, isAvailable, genres, totalEpisodes, episodeEachSeasonMap);
                    inventory.add(series);
                }
            }
            
        } catch (FileNotFoundException e) {
            System.out.println("File could not be found");
            e.printStackTrace();
            System.exit(0);
        } catch (IOException e) {
            System.out.println("Could not read file");
            e.printStackTrace();
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Something went wrong");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void writeToFile(String fileName, List<Content> contents) {
        try (BufferedWriter wr = new BufferedWriter(new FileWriter(fileName, true))) {

            for (Content c : contents) {
                wr.write(c.getContentId() + ", " + c.getTitle() + ", " + c.isAvailable());
                wr.newLine();
            }

        } catch (FileNotFoundException e) {
            System.out.println("File could not be found");
            e.printStackTrace();
            System.exit(0);
        } catch (IOException e) {
            System.out.println("Could not write to file");
            e.printStackTrace();
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Something went wrong");
            e.printStackTrace();
            System.exit(0);
        }
    }
    
    public void processReturn(int contentId, Customer customer) {
        Content contentToReturn = this.getContentById(contentId);

        if (!contentToReturn.isAvailable()) {
            contentToReturn.setAvailable(true);
            
            customer.addToHistory(contentToReturn);

            System.out.println(customer.getName() + " has successfully return: " + contentToReturn.getTitle()
            +"\nOn " + LocalDate.now());
        } else {
            System.out.println("Returning content failed");
        }
        
    }
    

    public Content getContentById(int id) {
        for (Content content : inventory) {
            if (content.getContentId() == id) {
                return content;
            }
        }

        return null;
    }

    public void searchByGenre(String genre) {
        boolean found = false;
        for (Content content : inventory) {
            if (content.getGenres().stream().anyMatch(g -> g.equalsIgnoreCase(genre))) {
                System.out.println("- " + content.getTitle());
                found = true;
            }
        }
        if (!found) {
            System.out.println("No movies found in the genre " + genre);
        }
    }

    public void searchByTitle(String filmTitle) {
        for (Content content : inventory) {
            if (content.getTitle().equalsIgnoreCase(filmTitle)) {
                content.printFullDetails();
                return;
            }
        }
        System.out.println("No movie with the title '" + filmTitle + "' was found.");
    }

    public Set<String> getCategories() {
        return categories;
    }
    
}
