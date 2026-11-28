package vn.edu.quanlydaotao.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import vn.edu.quanlydaotao.entity.Major;
import vn.edu.quanlydaotao.repository.MajorRepository;

@Component
@Order(1)
public class MajorDataInitializer implements ApplicationRunner {

	private static final List<MajorSeed> INITIAL_MAJORS = List.of(
			new MajorSeed("CNTT", "Công nghệ thông tin",
					"Đào tạo về lập trình, phát triển phần mềm và quản trị hệ thống."),
			new MajorSeed("QTKD", "Quản trị kinh doanh",
					"Đào tạo về quản lý, điều hành và phát triển hoạt động kinh doanh."),
			new MajorSeed("LOG", "Logistics và Quản lý chuỗi cung ứng",
					"Đào tạo về vận tải, kho bãi và quản lý chuỗi cung ứng."),
			new MajorSeed("KT", "Kế toán", "Đào tạo về kế toán, kiểm toán và quản lý tài chính."),
			new MajorSeed("MKT", "Marketing", "Đào tạo về quảng cáo, truyền thông và nghiên cứu thị trường."),
			new MajorSeed("NNA", "Ngôn ngữ Anh",
					"Đào tạo kỹ năng sử dụng tiếng Anh trong giao tiếp và công việc."),
			new MajorSeed("TMĐT", "Thương mại điện tử",
					"Đào tạo về kinh doanh và giao dịch trên nền tảng trực tuyến."),
			new MajorSeed("TKDH", "Thiết kế đồ họa",
					"Đào tạo về thiết kế hình ảnh, nhận diện thương hiệu và truyền thông."));

	private final MajorRepository majorRepository;

	public MajorDataInitializer(MajorRepository majorRepository) {
		this.majorRepository = majorRepository;
	}

	@Override
	public void run(ApplicationArguments args) {
		Set<String> existingCodes = majorRepository.findAll().stream()
				.map(major -> normalizeCode(major.getMajorCode()))
				.collect(Collectors.toSet());
		List<Major> missingMajors = INITIAL_MAJORS.stream()
				.filter(major -> !existingCodes.contains(normalizeCode(major.code())))
				.map(MajorDataInitializer::toEntity)
				.toList();
		if (!missingMajors.isEmpty()) {
			majorRepository.saveAll(missingMajors);
		}
	}

	private static Major toEntity(MajorSeed seed) {
		Major major = new Major();
		major.setMajorCode(seed.code());
		major.setMajorName(seed.name());
		major.setDescription(seed.description());
		major.setActive(true);
		return major;
	}

	private static String normalizeCode(String code) {
		return code.toUpperCase(Locale.ROOT).replace('Ð', 'Đ');
	}

	private record MajorSeed(String code, String name, String description) {
	}
}
