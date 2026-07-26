package com.bookstore.bookstore.infrastructure.persistence;

import java.util.List;

final class DevelopmentSeedCatalog {

    static final int DEFAULT_CATEGORY_COUNT = 12;
    static final int MIN_CATEGORY_COUNT = 6;

    private static final List<String> BOOK_DESCRIPTION_TEMPLATES = List.of(
            "%1$s là nhan đề tiêu biểu của %2$s, phù hợp với độc giả yêu thích dòng %3$s.",
            "Trong %1$s, %2$s mang đến nhịp kể giàu cảm xúc và tinh thần rất đặc trưng của thể loại %3$s.",
            "%1$s được chọn cho tủ sách SáchVui nhờ giá trị lâu bền, nội dung dễ tiếp cận và sức hút của %3$s.",
            "%2$s xây dựng ở %1$s một tác phẩm nổi bật, thường được độc giả tìm đọc khi bắt đầu với dòng %3$s.",
            "Ấn bản %1$s giúp danh mục %3$s của SáchVui có thêm một đầu sách đáng đọc của %2$s.",
            "%1$s là lựa chọn phù hợp cho bạn đọc muốn khám phá chiều sâu, bối cảnh và phong cách riêng của %2$s trong nhóm %3$s.",
            "Từ nội dung đến giá trị lưu trữ, %1$s luôn nằm trong nhóm sách %3$s được quan tâm nhiều trên kệ của SáchVui.",
            "%2$s để lại dấu ấn rõ nét trong %1$s, một đầu sách %3$s thường được chọn cho tủ sách gia đình."
    );

    private static final List<String> SUPPLIER_SPECIALTIES = List.of(
            "văn học dịch và tiểu thuyết bán chạy",
            "sách thiếu nhi và giáo dục sớm",
            "sách kỹ năng, kinh doanh và quản trị",
            "ấn bản bìa cứng, sách quà tặng và sách minh họa",
            "sách lịch sử, hồi ký và tư liệu chuyên đề",
            "sách khoa học phổ thông và tri thức ứng dụng",
            "sách văn hóa, nghệ thuật và phong cách sống",
            "sách học ngoại ngữ, tham khảo và ôn luyện"
    );

    static final List<CategorySeed> CATEGORIES = List.of(
            category("LITERATURE", "Văn học", "Tiểu thuyết, truyện ngắn và các tác phẩm văn chương nổi bật.",
                    "Literature", "Novels, short stories, and notable literary works."),
            category("PERSONAL_DEVELOPMENT", "Kỹ năng & phát triển bản thân", "Sách ứng dụng giúp cải thiện tư duy, giao tiếp và thói quen.",
                    "Skills & Personal Development", "Practical books for improving mindset, communication, and daily habits."),
            category("CHILDREN", "Thiếu nhi", "Tác phẩm giàu trí tưởng tượng dành cho trẻ em và gia đình.",
                    "Children's Books", "Imaginative books for children and families."),
            category("BUSINESS_MANAGEMENT", "Kinh doanh & quản trị", "Kiến thức về lãnh đạo, vận hành, tài chính và khởi nghiệp.",
                    "Business & Management", "Books on leadership, operations, finance, and entrepreneurship."),
            category("SCIENCE_TECHNOLOGY", "Khoa học & công nghệ", "Kiến thức khoa học phổ thông, công nghệ và khám phá tự nhiên.",
                    "Science & Technology", "Accessible science, technology, and discoveries about the natural world."),
            category("HISTORY_MEMOIR", "Lịch sử & hồi ký", "Những câu chuyện có thật, ký ức cá nhân và các giai đoạn lịch sử.",
                    "History & Memoir", "True stories, personal memories, and accounts of historical periods."),
            category("FANTASY", "Giả tưởng & kỳ ảo", "Thế giới phép thuật, thần thoại và những chuyến phiêu lưu kỳ ảo.",
                    "Fantasy", "Magical worlds, mythology, and extraordinary adventures."),
            category("MYSTERY", "Trinh thám", "Các vụ án, bí ẩn và hành trình truy tìm sự thật.",
                    "Mystery & Crime", "Cases, mysteries, and journeys in search of the truth."),
            category("PSYCHOLOGY", "Tâm lý học", "Khám phá cảm xúc, hành vi và cách con người đưa ra quyết định.",
                    "Psychology", "Explore emotions, behavior, and how people make decisions."),
            category("PHILOSOPHY", "Triết học", "Tư tưởng kinh điển và những câu hỏi nền tảng về cuộc sống.",
                    "Philosophy", "Classic ideas and fundamental questions about life."),
            category("CONTEMPORARY_LITERATURE", "Văn học đương đại", "Tác phẩm hiện đại phản ánh con người và xã hội hôm nay.",
                    "Contemporary Literature", "Modern works reflecting people and society today."),
            category("SCIENCE_FICTION", "Khoa học viễn tưởng", "Tương lai, không gian và tác động của khoa học lên con người.",
                    "Science Fiction", "The future, space, and the impact of science on humanity."),
            category("EDUCATION", "Giáo dục", "Tài liệu học tập và phương pháp giáo dục dành cho nhiều lứa tuổi.",
                    "Education", "Learning resources and educational methods for different age groups."),
            category("ART_CREATIVITY", "Nghệ thuật & sáng tạo", "Mỹ thuật, thiết kế, nhiếp ảnh và thực hành sáng tạo.",
                    "Art & Creativity", "Art, design, photography, and creative practice."),
            category("TRAVEL_EXPLORATION", "Du ký & khám phá", "Hành trình, địa danh và trải nghiệm văn hóa trên thế giới.",
                    "Travel & Exploration", "Journeys, destinations, and cultural experiences around the world."),
            category("FOOD_LIFESTYLE", "Ẩm thực & phong cách sống", "Món ăn, chăm sóc nhà cửa và lối sống cân bằng.",
                    "Food & Lifestyle", "Cooking, home care, and a balanced way of living.")
    );

    static final List<AuthorSeed> AUTHORS = List.of(
            author("Jane Austen",
                    "Nhà văn Anh nổi tiếng với những tiểu thuyết về tình yêu, giai tầng và phép ứng xử trong xã hội Anh đầu thế kỷ XIX.",
                    1775, 1817, null),
            author("J. K. Rowling",
                    "Nhà văn Anh, tác giả loạt Harry Potter có sức ảnh hưởng lớn đến văn học thiếu niên đương đại.",
                    1965, null, null),
            author("Emily Brontë",
                    "Nhà văn Anh, tác giả Đồi gió hú với giọng văn dữ dội và giàu cảm xúc.",
                    1818, 1848, null),
            author("Dale Carnegie",
                    "Tác giả người Mỹ nổi tiếng trong mảng giao tiếp, lãnh đạo và phát triển bản thân.",
                    1888, 1955, null),
            author("George Orwell",
                    "Nhà văn và nhà báo Anh, nổi bật với các tác phẩm phê phán quyền lực và chủ nghĩa toàn trị.",
                    1903, 1950, null),
            author("Antoine de Saint-Exupéry",
                    "Nhà văn kiêm phi công người Pháp, được nhớ đến rộng rãi qua Hoàng tử bé.",
                    1900, 1944, null),
            author("J. R. R. Tolkien",
                    "Nhà văn, học giả người Anh và là người đặt nền móng cho fantasy hiện đại.",
                    1892, 1973, null),
            author("Paulo Coelho",
                    "Nhà văn Brazil nổi tiếng toàn cầu với những tiểu thuyết giàu tính chiêm nghiệm và hành trình tinh thần.",
                    1947, null, null),
            author("William Shakespeare",
                    "Đại thi hào Anh với các vở kịch và bi kịch kinh điển của văn học thế giới.",
                    1564, 1616, null),
            author("Mark Twain",
                    "Nhà văn Mỹ, bậc thầy châm biếm và là tác giả của nhiều tác phẩm thiếu niên kinh điển.",
                    1835, 1910, null),
            author("Charles Dickens",
                    "Nhà văn Anh chuyên khắc họa đời sống đô thị, bất công xã hội và những số phận khó quên.",
                    1812, 1870, null),
            author("Suzanne Collins",
                    "Nhà văn Mỹ nổi tiếng với các tác phẩm dystopia dành cho độc giả trẻ.",
                    1962, null, null),
            author("L. Frank Baum",
                    "Tác giả người Mỹ, cha đẻ của thế giới Oz giàu trí tưởng tượng.",
                    1856, 1919, null),
            author("E. L. James",
                    "Nhà văn Anh được biết đến rộng rãi với các tiểu thuyết tình cảm đương đại gây nhiều tranh luận.",
                    1963, null, null),
            author("Albert Camus",
                    "Nhà văn, triết gia Pháp gắn liền với chủ nghĩa phi lý và tư tưởng hiện sinh.",
                    1913, 1960, null),
            author("Jeff Kinney",
                    "Tác giả kiêm họa sĩ người Mỹ, nổi tiếng với loạt Nhật ký chú bé nhút nhát.",
                    1971, null, null),
            author("Stephenie Meyer",
                    "Nhà văn Mỹ được biết đến qua các tiểu thuyết giả tưởng lãng mạn dành cho tuổi teen.",
                    1973, null, null),
            author("Charlotte Brontë",
                    "Nhà văn Anh, tác giả Jane Eyre và là gương mặt quan trọng của văn học Victoria.",
                    1816, 1855, null),
            author("Rick Riordan",
                    "Nhà văn Mỹ nổi bật với các series phiêu lưu thần thoại dành cho thiếu niên.",
                    1964, null, null),
            author("E. B. White",
                    "Nhà văn Mỹ nổi tiếng với văn học thiếu nhi giàu sự dịu dàng và tinh tế.",
                    1899, 1985, null),
            author("Dan Brown",
                    "Tác giả Mỹ chuyên viết thriller pha trộn mật mã, nghệ thuật và tôn giáo.",
                    1964, null, null),
            author("Anne Frank",
                    "Tác giả nhật ký người Do Thái gốc Đức, biểu tượng của ký ức Holocaust.",
                    1929, 1945, null),
            author("Alexandre Dumas",
                    "Nhà văn Pháp nổi tiếng với các tiểu thuyết phiêu lưu lịch sử quy mô lớn.",
                    1802, 1870, null),
            author("Miguel de Cervantes",
                    "Nhà văn Tây Ban Nha, tác giả Don Quixote và là tượng đài của văn học châu Âu.",
                    1547, 1616, null),
            author("William Golding",
                    "Nhà văn Anh, Nobel Văn học, nổi tiếng với các tác phẩm về bản năng và trật tự xã hội.",
                    1911, 1993, null),
            author("Ernest Hemingway",
                    "Nhà văn Mỹ với phong cách tối giản và nhiều tác phẩm kinh điển về chiến tranh, tình yêu và nghị lực.",
                    1899, 1961, null),
            author("John Steinbeck",
                    "Nhà văn Mỹ đoạt Nobel với nhiều tác phẩm hiện thực về đời sống lao động và khủng hoảng xã hội.",
                    1902, 1968, null),
            author("Agatha Christie",
                    "Nữ hoàng trinh thám người Anh với hàng chục vụ án kinh điển trong văn học đại chúng.",
                    1890, 1976, null),
            author("Stephen Hawking",
                    "Nhà vật lý lý thuyết người Anh, nổi tiếng vì khả năng phổ biến khoa học tới công chúng rộng rãi.",
                    1942, 2018, null),
            author("Kenneth Grahame",
                    "Nhà văn Anh gắn liền với những tác phẩm thiếu nhi ấm áp và giàu chất đồng quê.",
                    1859, 1932, null),
            author("Markus Zusak",
                    "Nhà văn Úc nổi tiếng với các tiểu thuyết giàu cảm xúc về tuổi trẻ và chiến tranh.",
                    1975, null, null),
            author("Lão Tử",
                    "Nhân vật triết học cổ đại Trung Hoa, gắn với Đạo Đức Kinh và tư tưởng Đạo gia.",
                    null, null, null),
            author("Victor Hugo",
                    "Đại văn hào Pháp với các tiểu thuyết quy mô lớn về công lý, tình thương và xã hội.",
                    1802, 1885, null),
            author("Gustave Flaubert",
                    "Nhà văn Pháp đặt nền móng cho chủ nghĩa hiện thực hiện đại với phong cách câu chữ chặt chẽ.",
                    1821, 1880, null),
            author("C. S. Lewis",
                    "Nhà văn và học giả Anh nổi tiếng với Biên niên sử Narnia và các trước tác Kitô giáo.",
                    1898, 1963, null),
            author("Eric Carle",
                    "Tác giả, họa sĩ người Mỹ nổi tiếng với sách tranh thiếu nhi giàu màu sắc và nhịp điệu.",
                    1929, 2021, null),
            author("Mitch Albom",
                    "Nhà văn, nhà báo Mỹ chuyên viết những tác phẩm truyền cảm hứng về tình người và sự trưởng thành.",
                    1958, null, null),
            author("Stephen Chbosky",
                    "Nhà văn, biên kịch Mỹ được biết đến với các tác phẩm tuổi mới lớn giàu sự đồng cảm.",
                    1970, null, null),
            author("Alice Walker",
                    "Nhà văn Mỹ đoạt Pulitzer, nổi bật với các tác phẩm về nữ quyền và trải nghiệm của người da màu.",
                    1944, null, null),
            author("Haruki Murakami",
                    "Nhà văn Nhật Bản nổi tiếng với phong cách siêu thực, cô độc và giàu chất âm nhạc.",
                    1949, null, null),
            author("Gabriel García Márquez",
                    "Nhà văn Colombia, Nobel Văn học, biểu tượng của chủ nghĩa hiện thực huyền ảo.",
                    1927, 2014, null),
            author("Franz Kafka",
                    "Nhà văn viết tiếng Đức sinh tại Praha, nổi tiếng với những thế giới bất an và phi lý.",
                    1883, 1924, null),
            author("Leo Tolstoy",
                    "Đại văn hào Nga với các tiểu thuyết sử thi và suy tư đạo đức sâu sắc.",
                    1828, 1910, null),
            author("Fyodor Dostoevsky",
                    "Nhà văn Nga chuyên đào sâu tâm lý, tội lỗi, đức tin và sự cứu rỗi.",
                    1821, 1881, null),
            author("Yuval Noah Harari",
                    "Sử gia và tác giả Israel nổi tiếng với các sách phổ thông về lịch sử, công nghệ và tương lai loài người.",
                    1976, null, null),
            author("Khaled Hosseini",
                    "Nhà văn gốc Afghanistan với các tiểu thuyết giàu cảm xúc về gia đình, chiến tranh và tha hương.",
                    1965, null, null),
            author("Kazuo Ishiguro",
                    "Nhà văn Anh gốc Nhật, Nobel Văn học, nổi tiếng với giọng kể tiết chế và ám ảnh ký ức.",
                    1954, null, null),
            author("Isabel Allende",
                    "Nhà văn Chile có nhiều tiểu thuyết lịch sử - gia đình giàu chất kể chuyện.",
                    1942, null, null),
            author("Orhan Pamuk",
                    "Nhà văn Thổ Nhĩ Kỳ đoạt Nobel, nổi bật với các tác phẩm về ký ức đô thị, bản sắc và giao thoa Đông - Tây.",
                    1952, null, null),
            author("Sally Rooney",
                    "Nhà văn Ireland đương đại nổi tiếng với những tiểu thuyết về tình yêu, giao tiếp và thế hệ trẻ.",
                    1991, null, null)
    );

    static final List<PublisherSeed> PUBLISHERS = List.of(
            publisher("NXB Trẻ", "Nhà xuất bản có thế mạnh về văn học, kỹ năng và sách dành cho bạn đọc trẻ."),
            publisher("NXB Kim Đồng", "Đơn vị xuất bản sách thiếu nhi và văn học tuổi mới lớn."),
            publisher("NXB Hội Nhà Văn", "Nhà xuất bản chuyên về tác phẩm văn học trong nước và quốc tế."),
            publisher("NXB Văn Học", "Đơn vị xuất bản các tác phẩm văn học kinh điển và đương đại."),
            publisher("NXB Thế Giới", "Nhà xuất bản đa lĩnh vực với nhiều đầu sách dịch chất lượng."),
            publisher("NXB Lao Động", "Đơn vị xuất bản sách kỹ năng, kinh tế và kiến thức ứng dụng."),
            publisher("NXB Dân Trí", "Nhà xuất bản sách phổ thông, giáo dục và phát triển bản thân."),
            publisher("NXB Tổng Hợp TP.HCM", "Nhà xuất bản đa ngành phục vụ bạn đọc trên cả nước."),
            publisher("NXB Phụ Nữ Việt Nam", "Đơn vị xuất bản văn học, gia đình và phong cách sống."),
            publisher("NXB Thanh Niên", "Nhà xuất bản sách kỹ năng, văn hóa và giáo dục thanh thiếu niên."),
            publisher("Nhã Nam", "Thương hiệu sách văn học dịch và văn học đương đại được nhiều độc giả lựa chọn."),
            publisher("First News - Trí Việt", "Đơn vị phát hành sách kỹ năng và truyền cảm hứng."),
            publisher("Alpha Books", "Thương hiệu sách quản trị, kinh doanh và tư duy."),
            publisher("Đông A", "Đơn vị xuất bản các ấn bản văn học và lịch sử được đầu tư công phu."),
            publisher("Omega Plus", "Thương hiệu sách tri thức, khoa học và lịch sử."),
            publisher("Thái Hà Books", "Thương hiệu sách kỹ năng, kinh doanh và chữa lành được bạn đọc trẻ quan tâm."),
            publisher("MCBooks", "Đơn vị phát hành sách ngoại ngữ, tham khảo và kỹ năng học tập."),
            publisher("Tân Việt Books", "Thương hiệu sách thiếu nhi, văn học và tri thức phổ thông."),
            publisher("IPM", "Đơn vị phát hành mạnh ở mảng văn học trẻ, manga và light novel có bản quyền."),
            publisher("Bách Việt Books", "Thương hiệu sách văn học đương đại, tình cảm và kỹ năng mềm."),
            publisher("Skybooks", "Dòng sách hướng đến độc giả trẻ, nổi bật với tản văn và tiểu thuyết đương đại."),
            publisher("Bloom Books", "Thương hiệu sách trẻ trung, tập trung vào tiểu thuyết và sách xu hướng."),
            publisher("Sống", "Dòng sách kỹ năng và chữa lành dành cho người trẻ."),
            publisher("Saigon Books", "Đơn vị phát hành văn học, lịch sử và sách kỹ năng ứng dụng."),
            publisher("AZ Việt Nam", "Thương hiệu phát hành văn học giải trí, manga và sách tuổi teen."),
            publisher("Crabit Kidbooks", "Đơn vị chuyên sách tranh và sách thiếu nhi có yếu tố giáo dục."),
            publisher("Quảng Văn", "Thương hiệu sách văn học dịch và sách dành cho phụ nữ, gia đình."),
            publisher("Phanbook", "Đơn vị phát hành sách văn học, giải trí và văn hóa đại chúng."),
            publisher("Chibooks", "Thương hiệu sách văn học, du ký và bản quyền quốc tế."),
            publisher("1980 Books", "Đơn vị phát hành sách tư duy, triết học và phát triển cá nhân."),
            publisher("Văn Lang Books", "Thương hiệu sách kỹ năng, thiếu nhi và tri thức phổ thông."),
            publisher("Minh Thắng Books", "Đơn vị phát hành sách học tập, tham khảo và kỹ năng."),
            publisher("BizBooks", "Dòng sách kinh doanh, quản trị và marketing thực tiễn."),
            publisher("PandaBooks", "Thương hiệu sách phổ thông hướng tới gia đình và độc giả trẻ."),
            publisher("Read & Lead", "Dòng sách kinh doanh và phát triển năng lực lãnh đạo."),
            publisher("Huy Hoàng Books", "Đơn vị phát hành sách văn học, lịch sử và giáo dục kỹ năng."),
            publisher("Đinh Tị Books", "Thương hiệu mạnh ở mảng thiếu nhi, gia đình và sách quà tặng."),
            publisher("Bookland", "Đơn vị phát hành sách phổ thông, ngoại ngữ và kỹ năng sống."),
            publisher("Times Books", "Thương hiệu sách đương đại, kỹ năng và giáo dục ứng dụng."),
            publisher("Lion Books", "Đơn vị phát hành sách phát triển bản thân và tri thức đại chúng."),
            publisher("NXB Giáo Dục Việt Nam", "Nhà xuất bản chủ lực trong mảng sách giáo khoa, tham khảo và học liệu."),
            publisher("NXB Chính Trị Quốc Gia Sự Thật", "Đơn vị xuất bản sách chính luận, lịch sử và tư liệu nghiên cứu."),
            publisher("NXB Đại Học Quốc Gia Hà Nội", "Nhà xuất bản học thuật phục vụ đào tạo, nghiên cứu và phổ biến tri thức."),
            publisher("NXB Đại Học Quốc Gia TP.HCM", "Đơn vị xuất bản giáo trình, chuyên khảo và sách tri thức ứng dụng."),
            publisher("NXB Tài Chính", "Nhà xuất bản chuyên về kinh tế, thuế, kế toán và tài chính doanh nghiệp."),
            publisher("NXB Mỹ Thuật", "Đơn vị xuất bản sách hội họa, thiết kế, nhiếp ảnh và sáng tạo."),
            publisher("NXB Khoa Học Và Kỹ Thuật", "Nhà xuất bản phục vụ sách chuyên ngành kỹ thuật và khoa học ứng dụng."),
            publisher("NXB Y Học", "Đơn vị xuất bản tài liệu sức khỏe, y khoa và chăm sóc cộng đồng."),
            publisher("NXB Thông Tin Và Truyền Thông", "Nhà xuất bản chuyên về công nghệ, truyền thông và chuyển đổi số."),
            publisher("NXB Tri Thức", "Đơn vị nổi bật với sách hàn lâm, triết học và khoa học xã hội.")
    );

    static final List<SupplierSeed> SUPPLIERS = List.of(
            supplier("Công ty CP Phát hành sách TP.HCM - FAHASA", "60-62 Lê Lợi, Quận 1, TP.HCM", "02838225796", "donhang@fahasa.com.vn"),
            supplier("Công ty CP Văn hóa Phương Nam", "940 Đường 3 Tháng 2, Quận 11, TP.HCM", "02838663447", "kinhdoanh@pnc.com.vn"),
            supplier("Công ty CP Văn hóa và Truyền thông Nhã Nam", "59 Đỗ Quang, Cầu Giấy, Hà Nội", "02435146875", "doitac@nhanam.vn"),
            supplier("Công ty CP Sách Alpha", "176 Thái Hà, Đống Đa, Hà Nội", "02437226234", "sales@alphabooks.vn"),
            supplier("Công ty CP Sách Thái Hà", "53 Phạm Thận Duật, Cầu Giấy, Hà Nội", "02437932268", "phanphoi@thaihabooks.com"),
            supplier("Công ty TNHH Văn hóa Đông A", "113 Đông Các, Đống Đa, Hà Nội", "02438565520", "kinhdoanh@donga.vn"),
            supplier("Công ty TNHH MTV Nhà xuất bản Trẻ", "161B Lý Chính Thắng, Quận 3, TP.HCM", "02839316289", "phathanh@nxbtre.com.vn"),
            supplier("Công ty CP Phát hành sách Kim Đồng", "55 Quang Trung, Hai Bà Trưng, Hà Nội", "02439434730", "phathanh@nxbkimdong.com.vn"),
            supplier("Công ty TNHH Sách và Truyền thông Việt Nam", "18 Nguyễn Trường Tộ, Ba Đình, Hà Nội", "02439274888", "doitac@sachviet.vn"),
            supplier("Công ty CP Văn hóa Huy Hoàng", "110D Ngọc Hà, Ba Đình, Hà Nội", "02437344671", "kinhdoanh@huyhoangbook.vn"),
            supplier("Công ty TNHH Văn hóa Minh Long", "Lô 8 KCN Tân Triều, Thanh Trì, Hà Nội", "02435529212", "sales@minhlongbook.vn"),
            supplier("Công ty TNHH Văn hóa Đinh Tị", "Cụm CN Ngọc Hồi, Thanh Trì, Hà Nội", "02436891005", "phanphoi@dinhtibooks.vn"),
            supplier("Công ty CP Sách MCBooks", "26-28 Nguyễn Khắc Hiếu, Ba Đình, Hà Nội", "02437931586", "kinhdoanh@mcbooks.vn"),
            supplier("Công ty CP Văn hóa và Giáo dục Tân Việt", "313 Bạch Mai, Hai Bà Trưng, Hà Nội", "02439749785", "hoptac@tanvietbooks.vn"),
            supplier("Công ty CP Văn hóa và Truyền thông AZ Việt Nam", "15A Hoàng Cầu, Đống Đa, Hà Nội", "02435562233", "doitac@azbook.vn"),
            supplier("Công ty CP Sách Sài Gòn - Saigon Books", "473 Điện Biên Phủ, Bình Thạnh, TP.HCM", "02835123688", "kinhdoanh@saigonbooks.vn"),
            supplier("Công ty CP Sách IPM", "24 Hòa Mã, Hai Bà Trưng, Hà Nội", "02439726011", "banquyen@ipm.vn"),
            supplier("Công ty CP Sách Bách Việt", "65 Trần Quốc Toản, Hoàn Kiếm, Hà Nội", "02439449888", "sales@bachvietbooks.vn"),
            supplier("Công ty CP Sách Quảng Văn", "34 Tô Hiến Thành, Hai Bà Trưng, Hà Nội", "02439783939", "doitac@quangvanbooks.com"),
            supplier("Công ty TNHH Crabit Kidbooks", "42 Nguyễn Thị Minh Khai, Quận 1, TP.HCM", "02838211146", "partner@crabitbooks.com"),
            supplier("Công ty CP Sách BizBooks", "Tòa N01T3 Ngoại Giao Đoàn, Bắc Từ Liêm, Hà Nội", "02466583886", "kinhdoanh@bizbooks.vn"),
            supplier("Công ty CP Sách Skybooks", "Tầng 5, 125 Nguyễn Sơn, Long Biên, Hà Nội", "02473036686", "doitac@skybooks.vn"),
            supplier("Công ty CP Sách Bloom Books", "125 Nguyễn Sơn, Long Biên, Hà Nội", "02473036689", "contact@bloombooks.vn"),
            supplier("Công ty CP Sách Sống", "54A Nguyễn Chí Thanh, Đống Đa, Hà Nội", "02432002368", "phanphoi@songbooks.vn"),
            supplier("Công ty CP Sách Phanbook", "110-112 Bà Huyện Thanh Quan, Quận 3, TP.HCM", "02839305969", "sales@phanbook.vn"),
            supplier("Công ty CP Sách Chibooks", "40 Trần Quý Cáp, Đống Đa, Hà Nội", "02432233390", "doitac@chibooks.vn"),
            supplier("Công ty CP Sách 1980 Books", "20 Nguyễn Văn Huyên, Cầu Giấy, Hà Nội", "02473022680", "kinhdoanh@1980books.vn"),
            supplier("Công ty CP Văn hóa First News - Trí Việt", "11H Nguyễn Thị Minh Khai, Quận 1, TP.HCM", "02838223045", "hoptac@firstnews.com.vn"),
            supplier("Công ty CP Sách Omega Việt Nam", "176 Thái Hà, Đống Đa, Hà Nội", "02435378286", "sales@omegaplus.vn"),
            supplier("Công ty CP Sách PandaBooks", "125 Trần Duy Hưng, Cầu Giấy, Hà Nội", "02432126555", "contact@pandabooks.vn"),
            supplier("Công ty CP Sách Văn Lang", "45 Trần Hưng Đạo, Quận 1, TP.HCM", "02838299186", "doitac@vanlangbooks.com"),
            supplier("Công ty CP Sách Minh Thắng", "8 Tôn Thất Tùng, Đống Đa, Hà Nội", "02438523299", "kinhdoanh@minhthangbooks.vn"),
            supplier("Công ty CP Sách Lion Books", "105 Hoàng Quốc Việt, Cầu Giấy, Hà Nội", "02432001058", "partner@lionbooks.vn"),
            supplier("Công ty CP Sách Bookland", "92A Nguyễn Hữu Cảnh, Bình Thạnh, TP.HCM", "02835143456", "sales@bookland.vn"),
            supplier("Công ty CP Sách Times Books", "18 Tam Trinh, Hai Bà Trưng, Hà Nội", "02436322886", "doitac@timesbooks.vn"),
            supplier("Công ty CP Sách Read & Lead", "87 Nguyễn Khang, Cầu Giấy, Hà Nội", "02432118867", "contact@readandlead.vn"),
            supplier("Công ty CP Waka Digital Books", "Tầng 8, 98 Hoàng Quốc Việt, Cầu Giấy, Hà Nội", "02473059968", "partner@waka.vn"),
            supplier("Công ty TNHH Tiki Trading - Sách", "52 Út Tịch, Tân Bình, TP.HCM", "02873057171", "book-buyer@tiki.vn"),
            supplier("Công ty CP Sách Giáo dục ADCBook", "187 Giảng Võ, Đống Đa, Hà Nội", "02437361759", "kinhdoanh@adcbook.net.vn"),
            supplier("Công ty CP Sách Tiến Thọ", "424 Nguyễn Trãi, Thanh Xuân, Hà Nội", "02435522188", "partner@tiensach.vn"),
            supplier("Nhà xuất bản Giáo Dục Việt Nam - Trung tâm phát hành", "81 Trần Hưng Đạo, Hoàn Kiếm, Hà Nội", "02439422010", "phathanh@nxbgiaoduc.vn"),
            supplier("Nhà xuất bản Chính Trị Quốc Gia Sự Thật - Bộ phận kinh doanh", "6/86 Duy Tân, Cầu Giấy, Hà Nội", "02437934567", "kinhdoanh@stbook.vn"),
            supplier("Nhà xuất bản Đại Học Quốc Gia Hà Nội - Phòng phát hành", "144 Xuân Thủy, Cầu Giấy, Hà Nội", "02437547828", "phathanh@vnu.edu.vn"),
            supplier("Nhà xuất bản Đại Học Quốc Gia TP.HCM - Trung tâm sách", "Khu phố 6, Linh Trung, Thủ Đức, TP.HCM", "02837242160", "bookstore@vnuhcm.edu.vn"),
            supplier("Nhà xuất bản Tài Chính - Bộ phận đại lý", "58 Nguyễn Chí Thanh, Đống Đa, Hà Nội", "02437750363", "daily@nxbtaichinh.vn"),
            supplier("Nhà xuất bản Mỹ Thuật - Phòng phát hành", "44B Hàng Bài, Hoàn Kiếm, Hà Nội", "02439367518", "phathanh@nxbmythuat.vn"),
            supplier("Nhà xuất bản Khoa Học Và Kỹ Thuật - Kênh phân phối", "70 Trần Hưng Đạo, Hoàn Kiếm, Hà Nội", "02439422556", "kinhdoanh@nxbkhoahockythuat.vn"),
            supplier("Nhà xuất bản Y Học - Bộ phận bán buôn", "138A Giảng Võ, Ba Đình, Hà Nội", "02438465899", "daily@nxbhmed.vn"),
            supplier("Nhà xuất bản Thông Tin Và Truyền Thông - Trung tâm sách", "18 Nguyễn Du, Hai Bà Trưng, Hà Nội", "02439436888", "phathanh@nxbtttt.vn"),
            supplier("Nhà xuất bản Tri Thức - Bộ phận đối tác", "53 Nguyễn Du, Hai Bà Trưng, Hà Nội", "02439445680", "doitac@nxbtrithuc.com.vn")
    );

    static final List<PersonSeed> PEOPLE = List.of(
            person("Nguyễn", "Minh Anh", "minhanh.nguyen"), person("Trần", "Hoàng Nam", "hoangnam.tran"),
            person("Lê", "Thu Hà", "thuha.le"), person("Phạm", "Quang Huy", "quanghuy.pham"),
            person("Hoàng", "Ngọc Mai", "ngocmai.hoang"), person("Vũ", "Đức Anh", "ducanh.vu"),
            person("Đặng", "Khánh Linh", "khanhlinh.dang"), person("Bùi", "Gia Bảo", "giabao.bui"),
            person("Đỗ", "Thanh Trúc", "thanhtruc.do"), person("Hồ", "Tuấn Kiệt", "tuankiet.ho"),
            person("Ngô", "Phương Thảo", "phuongthao.ngo"), person("Dương", "Minh Khang", "minhkhang.duong"),
            person("Lý", "Bảo Ngọc", "baongoc.ly"), person("Trương", "Anh Tuấn", "anhtuan.truong"),
            person("Đinh", "Hải Yến", "haiyen.dinh"), person("Mai", "Nhật Minh", "nhatminh.mai"),
            person("Cao", "Thùy Dương", "thuyduong.cao"), person("Tạ", "Đăng Khoa", "dangkhoa.ta"),
            person("Lâm", "Mỹ Linh", "mylinh.lam"), person("Phan", "Trọng Nghĩa", "trongnghia.phan"),
            person("Nguyễn", "Thảo Vy", "thaovy.nguyen"), person("Trần", "Minh Triết", "minhtriet.tran"),
            person("Lê", "Kim Oanh", "kimoanh.le"), person("Phạm", "Hữu Phước", "huuphuoc.pham"),
            person("Hoàng", "Diệu Linh", "dieulinh.hoang"), person("Vũ", "Quốc Bảo", "quocbao.vu"),
            person("Đặng", "Tường Vi", "tuongvi.dang"), person("Bùi", "Thanh Tùng", "thanhtung.bui"),
            person("Đỗ", "Ngọc Hân", "ngochan.do"), person("Hồ", "Đình Phong", "dinhphong.ho"),
            person("Ngô", "Mai Chi", "maichi.ngo"), person("Dương", "Thành Đạt", "thanhdat.duong"),
            person("Lý", "Khả Hân", "khahan.ly"), person("Trương", "Văn Khôi", "vankhoi.truong"),
            person("Đinh", "Hoài An", "hoaian.dinh"), person("Mai", "Xuân Trường", "xuantruong.mai"),
            person("Cao", "Ánh Dương", "anhduong.cao"), person("Tạ", "Thiên Phúc", "thienphuc.ta"),
            person("Lâm", "Quỳnh Anh", "quynhanh.lam"), person("Phan", "Đức Thịnh", "ducthinh.phan"),
            person("Nguyễn", "Tú Uyên", "tuuyen.nguyen"), person("Trần", "Bảo Long", "baolong.tran"),
            person("Lê", "Thanh Vân", "thanhvan.le"), person("Phạm", "Minh Quân", "minhquan.pham"),
            person("Hoàng", "Yến Nhi", "yennhi.hoang"), person("Vũ", "Trung Hiếu", "trunghieu.vu"),
            person("Đặng", "Hà My", "hamy.dang"), person("Bùi", "Tiến Dũng", "tiendung.bui"),
            person("Đỗ", "Lan Phương", "lanphuong.do")
    );

    static final List<String> STREETS = List.of(
            "Nguyễn Thị Minh Khai", "Điện Biên Phủ", "Cách Mạng Tháng Tám", "Phan Xích Long",
            "Nguyễn Văn Linh", "Lê Văn Sỹ", "Hoàng Văn Thụ", "Tô Hiến Thành", "Võ Văn Tần",
            "Trần Hưng Đạo", "Xô Viết Nghệ Tĩnh", "Nguyễn Kiệm"
    );

    static final List<String> REVIEWS = List.of(
            "Bản dịch mạch lạc, nội dung cuốn hút và chất lượng in tốt.",
            "Sách được đóng gói cẩn thận, giao đúng hẹn và không bị móp góc.",
            "Một tác phẩm đáng đọc lại, đặc biệt phù hợp cho tủ sách gia đình.",
            "Nội dung có chiều sâu nhưng vẫn dễ theo dõi, phần chú thích khá hữu ích.",
            "Bìa đẹp, giấy tốt và cỡ chữ vừa mắt. Tôi hài lòng với lần mua này.",
            "Câu chuyện để lại nhiều suy ngẫm, nhịp kể càng về cuối càng hấp dẫn.",
            "Giá hợp lý so với chất lượng ấn bản và trải nghiệm đọc.",
            "Sách phù hợp làm quà tặng, hình thức chỉnh chu và nội dung có giá trị.",
            "Tôi thích cách trình bày gọn gàng và phần mục lục dễ tra cứu.",
            "Đây là cuốn sách tôi sẽ giới thiệu cho bạn bè sau khi đọc xong."
    );

    static final List<BookSeed> BOOKS = List.of(
            book("Kiêu hãnh và định kiến", "Jane Austen", 0, 10, "9781542625029", 14348537, 1813, 432, 128000),
            book("Harry Potter và Hòn đá Phù thủy", "J. K. Rowling", 6, 0, "9782075094450", 15155833, 1997, 368, 185000),
            book("Đồi gió hú", "Emily Brontë", 0, 10, "9780142423295", 12818862, 1847, 416, 145000),
            book("Đắc nhân tâm", "Dale Carnegie", 1, 11, "9787111124849", 13314878, 1936, 320, 86000),
            book("Trại súc vật", "George Orwell", 0, 3, "9781652775980", 11261770, 1945, 176, 79000),
            book("Hoàng tử bé", "Antoine de Saint-Exupéry", 2, 1, "9785882156922", 10708272, 1943, 144, 89000),
            book("Harry Potter và Phòng chứa Bí mật", "J. K. Rowling", 6, 0, "9780439451932", 15158664, 1998, 400, 195000),
            book("Người Hobbit", "J. R. R. Tolkien", 6, 13, "9780792443483", 14627509, 1937, 432, 168000),
            book("Nhà giả kim", "Paulo Coelho", 1, 10, "9788390423029", 7414780, 1988, 228, 88000),
            book("Romeo và Juliet", "William Shakespeare", 0, 3, "9789500301961", 8257991, 1597, 256, 98000),
            book("Harry Potter và Tên tù nhân ngục Azkaban", "J. K. Rowling", 6, 0, "9781781105665", 10580435, 1999, 480, 215000),
            book("Những cuộc phiêu lưu của Huckleberry Finn", "Mark Twain", 0, 3, "9798463590435", 8157718, 1884, 392, 135000),
            book("Chuyện hai thành phố", "Charles Dickens", 0, 13, "9798849341927", 13301713, 1859, 544, 158000),
            book("Đấu trường sinh tử", "Suzanne Collins", 11, 0, "9781481903219", 12646537, 2008, 384, 149000),
            book("Phù thủy xứ Oz", "L. Frank Baum", 2, 1, "9781911060260", 552443, 1900, 224, 92000),
            book("Năm mươi sắc thái", "E. L. James", 10, 8, "9781299091757", 12648183, 2011, 560, 178000),
            book("Harry Potter và Chiếc cốc lửa", "J. K. Rowling", 6, 0, "9781856137690", 12059372, 2000, 640, 245000),
            book("Người xa lạ", "Albert Camus", 9, 10, "9798846711082", 13151269, 1942, 184, 105000),
            book("Harry Potter và Hội Phượng Hoàng", "J. K. Rowling", 6, 0, "9789544467616", 15158666, 2003, 880, 285000),
            book("Nhật ký chú bé nhút nhát", "Jeff Kinney", 2, 1, "9782021011968", 14376136, 2007, 224, 99000),
            book("Chạng vạng", "Stephenie Meyer", 6, 8, "9780316014410", 12641977, 2005, 544, 168000),
            book("Jane Eyre", "Charlotte Brontë", 0, 10, "9783423240321", 8235363, 1847, 672, 175000),
            book("Kẻ cắp tia chớp", "Rick Riordan", 6, 1, "9781368098168", 7239831, 2005, 416, 138000),
            book("Oliver Twist", "Charles Dickens", 0, 3, "9798464861350", 13300802, 1838, 496, 148000),
            book("Charlotte và Wilbur", "E. B. White", 2, 1, "9783257250329", 8461797, 1952, 192, 85000),
            book("Mật mã Da Vinci", "Dan Brown", 7, 0, "9788497870801", 9255229, 2003, 592, 175000),
            book("Harry Potter và Hoàng tử lai", "J. K. Rowling", 6, 0, "9788183220743", 10716273, 2005, 672, 255000),
            book("Nhật ký Anne Frank", "Anne Frank", 5, 13, "9789387669208", 8584021, 1947, 352, 139000),
            book("Bá tước Monte Cristo", "Alexandre Dumas", 0, 13, "9787540210700", 14566393, 1844, 1248, 295000),
            book("Don Quixote", "Miguel de Cervantes", 0, 3, "9780061824562", 14428305, 1605, 1056, 268000),
            book("Chúa ruồi", "William Golding", 0, 10, "9780571371723", 8684447, 1954, 288, 119000),
            book("Ông già và biển cả", "Ernest Hemingway", 0, 3, "9798600502628", 463307, 1952, 160, 89000),
            book("Những cuộc phiêu lưu của Tom Sawyer", "Mark Twain", 2, 1, "9780812416824", 12043351, 1876, 320, 108000),
            book("Của chuột và người", "John Steinbeck", 0, 10, "9781517444853", 14319003, 1937, 176, 92000),
            book("Thuyết phục", "Jane Austen", 0, 10, "9798574450031", 12824691, 1817, 304, 125000),
            book("Mười người da đen nhỏ", "Agatha Christie", 7, 3, "9786287574632", 11172296, 1939, 288, 118000),
            book("Lược sử thời gian", "Stephen Hawking", 4, 14, "9780385365765", 10432365, 1988, 256, 159000),
            book("Gió qua rặng liễu", "Kenneth Grahame", 2, 1, "9781847496386", 13335427, 1908, 288, 108000),
            book("Kẻ trộm sách", "Markus Zusak", 10, 0, "9781407037219", 8153054, 2005, 608, 175000),
            book("Án mạng trên chuyến tàu tốc hành Phương Đông", "Agatha Christie", 7, 3, "9781547904266", 11100465, 1934, 304, 125000),
            book("Đạo Đức Kinh", "Lão Tử", 9, 14, "9781984055767", 662232, 1891, 208, 119000),
            book("Những người khốn khổ", "Victor Hugo", 0, 13, "9780141392608", 12721865, 1862, 1464, 325000),
            book("Bà Bovary", "Gustave Flaubert", 0, 3, "9798589232837", 12993424, 1857, 448, 145000),
            book("Thiên thần và Ác quỷ", "Dan Brown", 7, 0, "9788417031275", 11408459, 2000, 624, 185000),
            book("Chùm nho phẫn nộ", "John Steinbeck", 0, 13, "9780749303273", 12715902, 1939, 720, 198000),
            book("Sư tử, Phù thủy và cái Tủ áo", "C. S. Lewis", 6, 1, "9788804436775", 8441376, 1950, 240, 105000),
            book("Chú sâu háu ăn", "Eric Carle", 2, 1, "9788416126835", 7835968, 1969, 32, 69000),
            book("Những ngày thứ Ba với thầy Morrie", "Mitch Albom", 1, 11, "9785403034128", 12560417, 1997, 224, 108000),
            book("Điệu vũ bên lề", "Stephen Chbosky", 10, 8, "9785389061606", 14315052, 1999, 256, 129000),
            book("Màu tím", "Alice Walker", 0, 8, "9781568653501", 8564628, 1982, 304, 139000)
    );

    private DevelopmentSeedCatalog() {
    }

    static PersonSeed personAt(int zeroBasedIndex) {
        return PEOPLE.get(Math.floorMod(zeroBasedIndex, PEOPLE.size()));
    }

    static AuthorSeed authorByName(String name) {
        return AUTHORS.stream()
                .filter(author -> author.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unknown author in seed catalog: " + name));
    }

    static String addressAt(int oneBasedIndex) {
        String street = STREETS.get(Math.floorMod(oneBasedIndex - 1, STREETS.size()));
        int district = Math.floorMod(oneBasedIndex - 1, 12) + 1;
        return "%d %s, Quận %d, TP. Hồ Chí Minh".formatted(18 + oneBasedIndex * 3, street, district);
    }

    static String profileAvatarUrlAt(int zeroBasedIndex) {
        int normalized = Math.floorMod(zeroBasedIndex, 50);
        String collection = normalized % 2 == 0 ? "women" : "men";
        int portraitIndex = normalized + 1;
        return "https://randomuser.me/api/portraits/%s/%d.jpg".formatted(collection, portraitIndex);
    }

    static String reviewAt(int zeroBasedIndex) {
        return REVIEWS.get(Math.floorMod(zeroBasedIndex, REVIEWS.size()));
    }

    static String supplierNoteAt(int zeroBasedIndex) {
        String specialty = SUPPLIER_SPECIALTIES.get(Math.floorMod(zeroBasedIndex, SUPPLIER_SPECIALTIES.size()));
        return "Đối tác cung ứng " + specialty + " cho hệ thống kho và cửa hàng SáchVui.";
    }

    static String bookDescriptionAt(int zeroBasedIndex, BookSeed book, CategorySeed category) {
        String template = BOOK_DESCRIPTION_TEMPLATES.get(Math.floorMod(zeroBasedIndex, BOOK_DESCRIPTION_TEMPLATES.size()));
        return template.formatted(book.title(), book.author(), category.name());
    }

    static int resolveCategoryIndex(int preferredIndex, int availableCount) {
        if (preferredIndex < availableCount) {
            return preferredIndex;
        }
        return switch (preferredIndex) {
            case 6, 7, 10 -> 0;
            case 8, 9 -> 1;
            case 11 -> 4;
            default -> Math.floorMod(preferredIndex, availableCount);
        };
    }

    private static CategorySeed category(
            String code,
            String name,
            String description,
            String englishName,
            String englishDescription
    ) {
        return new CategorySeed(code, name, description, englishName, englishDescription);
    }

    private static AuthorSeed author(
            String name,
            String biography,
            Integer birthYear,
            Integer deathYear,
            String avatarUrl
    ) {
        return new AuthorSeed(name, biography, birthYear, deathYear, avatarUrl);
    }

    private static PublisherSeed publisher(String name, String description) {
        return new PublisherSeed(name, description);
    }

    private static SupplierSeed supplier(String name, String address, String phone, String email) {
        return new SupplierSeed(name, address, phone, email);
    }

    private static PersonSeed person(String lastName, String firstName, String username) {
        return new PersonSeed(lastName, firstName, username);
    }

    private static BookSeed book(
            String title,
            String author,
            int categoryIndex,
            int publisherIndex,
            String isbn,
            long coverId,
            int publicationYear,
            int pageCount,
            long price
    ) {
        return new BookSeed(title, author, categoryIndex, publisherIndex, isbn, coverId, publicationYear, pageCount, price);
    }

    record CategorySeed(
            String code,
            String name,
            String description,
            String englishName,
            String englishDescription
    ) {
    }

    record AuthorSeed(String name, String biography, Integer birthYear, Integer deathYear, String avatarUrl) {
    }

    record PublisherSeed(String name, String description) {
    }

    record SupplierSeed(String name, String address, String phone, String email) {
    }

    record PersonSeed(String lastName, String firstName, String username) {
        String email() {
            return username + "@sachvui.vn";
        }

        String fullName() {
            return lastName + " " + firstName;
        }
    }

    record BookSeed(
            String title,
            String author,
            int categoryIndex,
            int publisherIndex,
            String isbn,
            long coverId,
            int publicationYear,
            int pageCount,
            long price
    ) {
        String coverStorageKey() {
            return "public/seed/books/%s.jpg".formatted(isbn);
        }
    }
}
