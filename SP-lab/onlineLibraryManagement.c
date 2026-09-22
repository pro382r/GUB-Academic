// structured programming lab

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#define MAX_BOOKS 100

struct Book {
    char title[100];
    char author[100];
    int year;
};
struct Library {
    struct Book books[MAX_BOOKS];
    int count;
};

void addBook(struct Library *lib) {
    if (lib->count >= MAX_BOOKS) {
        printf("Library is full!\n");
        return;
    }
    printf("Enter title: ");
    scanf(" %[^\n]", lib->books[lib->count].title);
    printf("Enter author: ");
    scanf(" %[^\n]", lib->books[lib->count].author);
    printf("Enter year: ");
    scanf("%d", &lib->books[lib->count].year);

    lib->count++;
    printf("Book added successfully.\n");
}

void displayBooks(struct Library *lib) {
    if (lib->count == 0) {
        printf("No books in the library.\n");
        return;
    }

    printf("\nBooks in the library:\n");
    for (int i = 0; i < lib->count; i++) {
        printf("\nTitle: %s\n", lib->books[i].title);
        printf("Author: %s\n", lib->books[i].author);
        printf("Year: %d\n", lib->books[i].year);
    }
}

int main() {
    struct Library lib = {0};
    int choice;

    while (1) {
        printf("\nLibrary Management System\n");
        printf("1. Add Book\n");
        printf("2. Display Books\n");
        printf("3. Exit\n");
        printf("Enter your choice: ");
        scanf("%d", &choice);

        switch (choice) {
            case 1:
                addBook(&lib);
                break;
            case 2:
                displayBooks(&lib);
                break;
            case 3:
                printf("Exiting program.\n");
                exit(0);
            default:
                printf("Invalid choice!\n");
        }
    }

    return 0;
}